package de.olli.service;

import de.olli.model.Forecast;
import de.olli.model.Price;
import de.olli.model.Stock;
import de.olli.repository.PriceMongoDBReactiveRepo;
import de.olli.repository.StockBaseMongoReactiveRepo;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Created by olli on 06.09.2017.
 */
@Service
@Slf4j
public class AnalyzingService {

    private final static int FIRST_STEP = 1;
    private final static int SECOND_STEP = 5;
    private final static int THIRD_STEP = 10;
    private final static int FOURTH_STEP = 20;

    private final StockBaseMongoReactiveRepo stockBaseRepo;
    private final PriceMongoDBReactiveRepo priceRepo;

    private int firstStep;
    private int secondStep;
    private int thirdStep;
    private int fourthStep;

    public AnalyzingService(PriceMongoDBReactiveRepo priceRepo, StockBaseMongoReactiveRepo stockBaseRepo) {
        this.stockBaseRepo = stockBaseRepo;
        this.priceRepo = priceRepo;
        this.firstStep = FIRST_STEP;
        this.secondStep = SECOND_STEP;
        this.thirdStep = THIRD_STEP;
        this.fourthStep = FOURTH_STEP;
    }

    public double calculateStockRating(String stockId) {
        double result = 0;
        Stock stock = stockBaseRepo.findById(stockId).block();
        Flux<Price> prices = priceRepo.findByStockIdOrderByDayDesc(stockId, Pageable.unpaged());
        result += stock.getWeight1() * calculateGradient(prices.next().block().getPrice(), prices.skip(firstStep).blockFirst().getPrice(), firstStep);
        result += stock.getWeight2() * calculateGradient(prices.next().block().getPrice(), prices.skip(secondStep).blockFirst().getPrice(), secondStep);
        result += stock.getWeight4() * calculateGradient(prices.next().block().getPrice(), prices.skip(thirdStep).blockFirst().getPrice(), thirdStep);
        result += stock.getWeight4() * calculateGradient(prices.next().block().getPrice(), prices.skip(fourthStep).blockFirst().getPrice(), fourthStep);
        return result;
    }

    public List<Forecast> calculateForecasts(String stockId, int daysToForecast) {
        Stock stock = stockBaseRepo.findById(stockId).block();
        Flux<Price> prices = priceRepo.findByStockIdOrderByDayDesc(stockId, Pageable.unpaged());
        int[] steps = {firstStep, secondStep, thirdStep, fourthStep};
        int bestMatchesCount = 5;
        double currentPrice = prices.next().block().getPrice();

        List<Forecast> forecasts = Lists.newArrayList();
        forecasts.add(determineForecast(stock, prices, currentPrice, daysToForecast, steps, 2));
        forecasts.add(determineForecast(stock, prices, currentPrice, daysToForecast, steps, bestMatchesCount));
        forecasts.add(determineForecast(stock, prices, currentPrice, daysToForecast, steps, 10));
        steps = new int[] {1,2,5,10};
        forecasts.add(determineForecast(stock, prices, currentPrice, daysToForecast, steps, 2));
        forecasts.add(determineForecast(stock, prices, currentPrice, daysToForecast, steps, bestMatchesCount));
        forecasts.add(determineForecast(stock, prices, currentPrice, daysToForecast, steps, 10));
        return forecasts;
    }

    public Pair<Double, Double> calculateAverageMismatch(List<Price> prices, int daysToForecast, Stock stock, int[] steps, int bestMatchesCount) {
        double cumulativeMismatch = 0;
        double maxMismatch = 0;
        double minMismatch = Double.MAX_VALUE;
        for (int i = prices.size() - 1 - daysToForecast * steps[steps.length - 1]; i > 1 + daysToForecast; i--) {
            double[] gradients = new double[steps.length];
            for (int l = 0; l < steps.length; l++) {
                gradients[l] = calculateGradient(prices.get(i).getPrice(), prices.get(i + steps[l]).getPrice(), steps[l]);
            }
            double gradient = determineMostMatchingGradient(prices, gradients, daysToForecast, stock, steps, bestMatchesCount);
            double mismatch = Math.abs(calculateGradient(prices.get(i).getPrice(), prices.get(i - daysToForecast).getPrice(), daysToForecast) - gradient);
            cumulativeMismatch += mismatch;
            maxMismatch = Math.max(maxMismatch, mismatch);
            minMismatch = Math.min(minMismatch, mismatch);
        }
        return Pair.of((cumulativeMismatch / (prices.size() - 2 - daysToForecast - fourthStep)) * 100, maxMismatch * 100);
    }

    @Scheduled(cron = "0 10 16 * * MON-FRI")
    public void optimizeFactors() {
        log.info("Started optimizing factors.");
        stockBaseRepo.findAll().toStream().parallel().forEach(stock -> {
            log.info("Started optimizing factors for stock {}.", stock.getStockId());
            List<Price> prices = Lists.newArrayList(priceRepo.findByStockIdOrderByDayDesc(stock.getStockId(), Pageable.unpaged()).toIterable());
            optimizeFactorsForStockId(stock, prices);
            stockBaseRepo.save(stock);
            log.info("Optimized factors for stock {} to [{}, {}, {}, {}]", stock.getStockId(), stock.getWeight1(), stock.getWeight2(), stock.getWeight3(), stock.getWeight4());
        });
    }

    private Forecast determineForecast(Stock stock, Flux<Price> prices, double currentPrice, int daysToForecast, int[] steps, int bestMatchesCount) {
        double[] gradients = new double[steps.length];
        for (int i = 0; i < steps.length; i++) {
            gradients[i] = calculateGradient(currentPrice, prices.skip(steps[i]).blockFirst().getPrice(), steps[i]);
        }
        List<Price> list = Lists.newArrayList(prices.toIterable());
        double gradient = determineMostMatchingGradient(list, gradients, daysToForecast, stock, steps, bestMatchesCount);
        Pair<Double, Double> mismatch = calculateAverageMismatch(list, daysToForecast, stock, steps, bestMatchesCount);
        return new Forecast(stock.getStockId(), gradient * 100, mismatch.getFirst(), mismatch.getSecond(), daysToForecast, steps, bestMatchesCount);
    }

    private double calculateGradient(double current, double reference, int days) {
        return ((current - reference) / days) / current;
    }

    private double determineMostMatchingGradient(List<Price> prices, double[] gradients, int daysToForecast, Stock stock, int[] steps, int bestMatchesCount) {
        Pair<Integer, Double>[] bestPredicts = new Pair[bestMatchesCount];
        for (int i = 1 + daysToForecast; i < prices.size() - daysToForecast * steps[steps.length - 1]; i++) {
            double localMismatch = 0;
            for (int l = 0; l < gradients.length; l++) {
                localMismatch += stock.getWeight1() * Math.abs(gradients[l] - calculateGradient(prices.get(i).getPrice(), prices.get(i + steps[l]).getPrice(), steps[l]));
            }
            if (bestPredicts[bestMatchesCount-1] == null || localMismatch < bestPredicts[bestMatchesCount-1].getSecond()) {
                bestPredicts = insertPair(i, localMismatch, bestPredicts);
            }
        }
        double cumulative = 0;
        double totalMismatch = 0;
        for(Pair<Integer, Double> bestPredict : bestPredicts) {
            totalMismatch += bestPredict.getSecond();
        }
        for(Pair<Integer, Double> bestPredict : bestPredicts) {
            double weight = 1 - (bestPredict.getSecond()/totalMismatch);
            cumulative += weight * calculateGradient(prices.get(bestPredict.getFirst()).getPrice(), prices.get(bestPredict.getFirst() - daysToForecast).getPrice(), daysToForecast);
        }
        return cumulative;
    }

    private Pair[] insertPair(int i, double mismatch, Pair<Integer, Double>[] bestPredicts) {
        Pair<Integer, Double> pairToInsert = Pair.of(i, mismatch);
        for (int l = 0; l < bestPredicts.length; l++) {
            if (bestPredicts[l] == null || mismatch < bestPredicts[l].getSecond()) {
                Pair tmp = bestPredicts[l];
                bestPredicts[l] = pairToInsert;
                pairToInsert = tmp;
            }
        }
        return bestPredicts;
    }

    private void optimizeFactorsForStockId(Stock stock, List<Price> prices) {
        double bestMismatch = Double.MAX_VALUE;
        double[] bestFactors = {stock.getWeight1(), stock.getWeight2(), stock.getWeight3(), stock.getWeight4()};
        int i = 0;
        while (i < 100) {
            log.info("loop #{}", i);
            double averageMismatch = calculateAverageMismatch(prices, 1, stock, new int[]{firstStep, secondStep, thirdStep, fourthStep}, 5).getFirst();
            if (averageMismatch < bestMismatch) {
                bestFactors[0] = stock.getWeight1();
                bestFactors[1] = stock.getWeight2();
                bestFactors[2] = stock.getWeight3();
                bestFactors[3] = stock.getWeight4();
                bestMismatch = averageMismatch;
            }
            adjustWeights(stock);
            i++;
        }
        stock.setWeight1(bestFactors[0]);
        stock.setWeight1(bestFactors[1]);
        stock.setWeight1(bestFactors[2]);
        stock.setWeight1(bestFactors[3]);
    }

    private void adjustWeights(Stock stock) {
        double first = Math.random() / 2;
        double second = Math.random() / 2;
        stock.setWeight1(first);
        stock.setWeight4(0.5 - first);
        stock.setWeight2(second);
        stock.setWeight3(0.5 - second);
    }
}
