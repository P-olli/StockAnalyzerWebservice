package de.olli.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.olli.model.Price;
import de.olli.model.Stock;
import de.olli.repository.StocksMongoDBReactiveRepository;
import de.olli.service.StockDataRetriever;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by olli on 21.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("offline")
public class StockControllerIT {

    private static final String STOCK_ID = "NDX1";
    private Stock stock;

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private StocksMongoDBReactiveRepository repository;

    @Test
    public void testGetStocksLocal() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        repository.deleteAll();
        stock = new Stock();
        stock.setId(STOCK_ID);
        stock.addPrice(new Price(STOCK_ID, now, 1d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(1), 2d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(2), 3d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(3), 4d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(4), 5d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(5), 6d));
        Flux<Price> savedPrices = repository.saveAll(stock.getPrices());
        savedPrices.blockFirst();

        webTestClient
                .get().uri("/stocks/NDX1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Price.class)
                .hasSize(6);
    }
}