package de.olli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by olli on 12.04.2017.
 */
@Service
public class StockService {

    private String aplhaUrl;
    private RestTemplate restTemplate;
    private Environment environment;
    private String apikey;

    @Autowired
    public StockService(@Value("${stock.url}") String aplhaUrl, RestTemplate restTemplate, Environment environment, @Value("${api.key}") String apikey) {
        this.aplhaUrl = aplhaUrl;
        this.restTemplate = restTemplate;
        this.environment = environment;
        this.apikey = apikey;
    }


    public List<Stock> getStocks(List<String> stockNames) {
        List<Stock> stockList = new ArrayList<>();
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(Predicate.isEqual("offline"))) {
            try {
                InputStream stream = ClassLoader.getSystemResourceAsStream("mock_data.json");

                ObjectMapper mapper = new ObjectMapper();
                stockList.add(mapper.readValue(stream, Stock.class));
                stockList.forEach(stock -> {
                    stock.setMovingAverage38(calculateAllPossibleMovingAverages(stock.getPrices(), 38));
                    stock.setMovingAverage100(calculateAllPossibleMovingAverages(stock.getPrices(), 100));
                    stock.setMovingAverage200(calculateAllPossibleMovingAverages(stock.getPrices(), 200));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String url = aplhaUrl + apikey;
            stockList = stockNames.parallelStream().map(stockName -> getStockFromApi(url, stockName)).collect(Collectors.toList());
        }
        return stockList;
    }

    @VisibleForTesting
    protected Stock getStockFromApi(String url, String stockName) {
        Stock stock = restTemplate.getForObject(url, Stock.class, stockName);
        stock.setMovingAverage38(calculateAllPossibleMovingAverages(stock.getPrices(), 38));
        stock.setMovingAverage100(calculateAllPossibleMovingAverages(stock.getPrices(), 100));
        stock.setMovingAverage200(calculateAllPossibleMovingAverages(stock.getPrices(), 200));
        return stock;
    }

    @VisibleForTesting
    protected Double calculateMovingAverage(List<Price> prices) {
        return prices.parallelStream().mapToDouble(Price::getPrice).average().getAsDouble();
    }

    protected List<Double> calculateAllPossibleMovingAverages(List<Price> prices, int period) {
        List<Double> allPossibleMovingAverages = Lists.newArrayList();
        for (int i = prices.size(); i > 0; i--) {
            allPossibleMovingAverages.add(0, calculateMovingAverage(prices.subList((i - period) >= 0 ? i - period : 0, i)));
        }
        return allPossibleMovingAverages;
    }

}
