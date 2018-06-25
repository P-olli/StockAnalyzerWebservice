package de.olli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.Stock;
import de.olli.repository.StocksElasticsearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by olli on 12.04.2017.
 */
@Service
@Slf4j
public class StockService {

    private String stockUrlDaily;
    private String stockUrlFull;
    private RestTemplate restTemplate;
    private StocksElasticsearchRepository repository;
    private Environment environment;

    @Autowired
    public StockService(@Value("${stock.url.daily}") String aplhaUrl, RestTemplate restTemplate, StocksElasticsearchRepository repository, Environment environment, @Value("${api.key}") String apikey) {
        this.stockUrlDaily = aplhaUrl + apikey;
        this.stockUrlFull = this.stockUrlDaily + "&outputsize=full";
        this.restTemplate = restTemplate;
        this.repository = repository;
        this.environment = environment;
    }


    public List<Stock> getStocks(List<String> stockNames) {
        List<Stock> stockList;
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(Predicate.isEqual("offline"))) {
            stockList = stockNames.parallelStream().map(stockName -> {
                long count = repository.countAllByStockId(stockName);
                log.info("Count: " + count);
                Iterable<Price> all = repository.findAll();
                log.info("size: " + Iterables.size(all));
                all.forEach(price -> log.info(price.toString()));
                if (count == 0) {
                    try {
                        InputStream stream = ClassLoader.getSystemResourceAsStream("mock_data.json");

                        ObjectMapper mapper = new ObjectMapper();
                        Stock stock = mapper.readValue(stream, Stock.class);
                        repository.saveAll(stock.getPrices());
                        return stock;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    Stock stock = new Stock();
                    stock.setId(stockName);
                    repository.streamPriceByStockId(stockName).sorted().forEach(stock::addPrice);
                    return stock;
                }
            }).collect(Collectors.toList());

        } else {
            stockList = stockNames.parallelStream().map(stockName -> {
                long count = repository.countAllByStockId(stockName);
                log.info("Count: " + count);
                Iterable<Price> all = repository.findAll();
                log.info("size: " + Iterables.size(all));
                all.forEach(price -> log.info(price.toString()));
                Price last = repository.getDistinctFirstByDayAndPrice(stockName);
                if (count == 0 || last.getDay().isBefore(LocalDateTime.now().minusDays(100))) {
                    Stock stock = getStockFromApi(stockName, true);
                    repository.saveAll(stock.getPrices());
                    return stock;
                } else {
                    Stock stock = getStockFromApi(stockName, false);
                }
                Stock stock = new Stock();
                stock.setId(stockName);
                repository.streamPriceByStockId(stockName).sorted().forEach(stock::addPrice);
                return stock;
            }).collect(Collectors.toList());
        }
        return stockList;
    }

    @VisibleForTesting
    private Stock getStockFromApi(String stockName, boolean full) {
        return full ? restTemplate.getForObject(stockUrlFull, Stock.class, stockName) : restTemplate.getForObject(stockUrlDaily, Stock.class, stockName);
    }

    protected void setMovingAverages(Stock stock) {
        stock.setMovingAverage38(calculateAllPossibleMovingAverages(stock.getPrices(), 38));
        stock.setMovingAverage100(calculateAllPossibleMovingAverages(stock.getPrices(), 100));
        stock.setMovingAverage200(calculateAllPossibleMovingAverages(stock.getPrices(), 200));
    }

    @VisibleForTesting
    private Double calculateMovingAverage(List<Price> prices) {
        return prices.parallelStream().mapToDouble(Price::getPrice).average().getAsDouble();
    }

    protected List<Double> calculateAllPossibleMovingAverages(List<Price> prices, int period) {
        List<Double> allPossibleMovingAverages = Lists.newArrayList();
        for (int i = prices.size(); i > 0; i--) {
            allPossibleMovingAverages.add(0, calculateMovingAverage(prices.subList((i - period) >= 0 ? i - period : 0, i)));
        }
        return allPossibleMovingAverages;
    }

    private void storeStocks(List<Stock> stocks) {
        stocks.parallelStream().forEach(stock -> stock.getPrices().parallelStream().forEach(price -> repository.save(price)));
    }

    private Stock getStockFromRepo(String stockName) {
        Stock stock = new Stock();
        stock.setId(stockName);
        repository.streamPriceByStockId(stockName).parallel().forEach(price -> stock.addPrice(price));
        return stock;
    }
}
