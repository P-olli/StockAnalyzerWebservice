package de.olli.service;

import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.PriceBase;
import de.olli.model.Stock;
import de.olli.model.dto.StockDto;
import de.olli.model.dto.StockCreationDto;
import de.olli.repository.StockBaseMongoReactiveRepo;
import de.olli.repository.IntradayPriceMongoDBReactiveRepo;
import de.olli.repository.PriceMongoDBReactiveRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by olli on 12.04.2017.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final PriceMongoDBReactiveRepo stocksRepo;
    private final IntradayPriceMongoDBReactiveRepo stocksIntradayRepo;
    private final StockBaseMongoReactiveRepo stockBaseRepo;
    private final StockDataRetriever stockDataRetriever;

    public void storeStockForRetrieval(StockCreationDto stockCreationDto) {
        Stock stock = Stock.builder()
                .stockId(stockCreationDto.getStockId())
                .weight1(0.25)
                .weight2(0.25)
                .weight3(0.25)
                .weight4(0.25)
                .build();
        stockBaseRepo.save(stock).block();
        storePricesFromApi(stock.getStockId(), true);
    }

    public void deleteStockForRetrieval(String stockId) {
        stockBaseRepo.deleteById(stockId).block();
    }

    public Flux<Stock> getAllStocks() {
        return stockBaseRepo.findAll();
    }

    public Flux<Price> getStock(String stockId, Pageable pageable) {
        Mono<Stock> stock = stockBaseRepo.findByStockId(stockId);
        if (stock.hasElement().block()) {
            final Flux<Price> alreadyStoredPrices = stocksRepo.findByStockIdOrderByDayDesc(stockId, pageable);
            return alreadyStoredPrices;
//            Mono<Price> latestPrice = stocksIntradayRepo.findFirstByStockIdAndDay(stockId);
  //          return alreadyStoredPrices.mergeOrderedWith(latestPrice, Comparator.naturalOrder());
        } else {
            throw new IllegalArgumentException("Stock <" + stockId + "> not stored.");
        }
    }

    public Flux<Price> getStockDaily(String stockId) {
        return stocksIntradayRepo.findAllByStockIdOrderByDayDesc(stockId);
    }

    //public Flux<Double> getMovingAverage(String stockId, int period) {
    //    return calculateAllPossibleMovingAverages(stocksRepo.findByStockIdOrderByDayDesc(stockId), period);
    //}

    @Async
    void storePricesFromApi(String stockName, boolean full) {
        Mono<StockDto> stockMono = stockDataRetriever.getStockFromApi(stockName, full);
        StockDto stockDto = stockMono.block();
        List<PriceBase> priceBases = stockDto.getPrices();
        stocksRepo.saveAll(stockDto != null ? Lists.transform(priceBases, PriceBase::toPrice) : null).blockFirst();
        log.info("Successfully saved {} prices", stockDto.getPrices().size());
    }

    @VisibleForTesting
    private Double calculateMovingAverage(List<Price> prices) {
        return prices.parallelStream().mapToDouble(Price::getPrice).average().getAsDouble();
    }

    protected Flux<Double> calculateAllPossibleMovingAverages(Flux<Price> priceFlux, int period) {
        List<Price> prices = priceFlux.collectList().block();
        List<Double> allPossibleMovingAverages = Lists.newArrayList();
        for (int i = prices.size(); i > 0; i--) {
            allPossibleMovingAverages.add(0, calculateMovingAverage(prices.subList((i - period) >= 0 ? i - period : 0, i)));
        }
        return Flux.fromIterable(allPossibleMovingAverages);
    }
}
