package de.olli.service;

import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.Stock;
import de.olli.repository.StocksMongoDBReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by olli on 12.04.2017.
 */
@Service
@Slf4j
public class StockService {

    private StocksMongoDBReactiveRepository reactiveRepository;
    private StockDataRetriever stockDataRetriever;

    @Autowired
    public StockService(StocksMongoDBReactiveRepository reactiveRepository, StockDataRetriever stockDataRetriever) {
        this.reactiveRepository = reactiveRepository;
        this.stockDataRetriever = stockDataRetriever;
    }


    public Flux<Price> getStock(String stockId) {
        Flux<Price> prices = reactiveRepository.findAllByStockIdOrderByDayDesc(stockId);
        if (!prices.count().blockOptional().isPresent() || prices.count().blockOptional().get() == 0) {
            prices = getStockFromApiAndStoreInDb(stockId);
        }
        return prices;
    }

    public Flux<Double> getMovingAverage(String stockId, int period) {
        return calculateAllPossibleMovingAverages(reactiveRepository.findAllByStockIdOrderByDayDesc(stockId), period);
    }

    private Flux<Price> getStockFromApiAndStoreInDb(String stockName) {
        Mono<Stock> stockMono = stockDataRetriever.getStockFromApi(stockName, true);
        List<Price> prices = stockMono.blockOptional().get().getPrices();
        reactiveRepository.saveAll(prices);
        return Flux.fromIterable(prices);
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
