package de.olli.controller;

import com.google.common.collect.Lists;
import de.olli.model.*;
import de.olli.model.dto.StockCreationDto;
import de.olli.model.dto.StockDto;
import de.olli.model.dto.StockTransactionDto;
import de.olli.repository.PriceMongoDBReactiveRepo;
import de.olli.repository.StockBaseMongoReactiveRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by olli on 21.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("offline")
public class StockControllerIT {

    private StockDto stockDto;

    @Autowired private WebTestClient webTestClient;
    @Autowired private PriceMongoDBReactiveRepo pricesRepository;
    @Autowired private StockBaseMongoReactiveRepo stocksRepo;

    @Test
    public void testGetStocksLocal() throws Exception {
        String stockId = UUID.randomUUID().toString();
        Mono<Stock> save = stocksRepo.save(Stock.builder().stockId(stockId).build());
        save.block();

        LocalDateTime now = LocalDateTime.now();
        pricesRepository.deleteAll();

        stockDto = new StockDto();
        stockDto.setId(stockId);
        stockDto.addPrice(new Price(stockId, now, 1d));
        stockDto.addPrice(new Price(stockId, now.minusDays(1), 2d));
        stockDto.addPrice(new Price(stockId, now.minusDays(2), 3d));
        stockDto.addPrice(new Price(stockId, now.minusDays(3), 4d));
        stockDto.addPrice(new Price(stockId, now.minusDays(4), 5d));
        stockDto.addPrice(new Price(stockId, now.minusDays(5), 6d));
        Flux<Price> savedPrices = pricesRepository.saveAll(Lists.transform(stockDto.getPrices(), PriceBase::toPrice));
        savedPrices.blockFirst();

        webTestClient
                .get().uri("/stocks/" + stockId + "?timeRange=daily")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Price.class)
                .hasSize(6);
    }

    @Test
    public void testStoreStockForRetrieval() {
        String stockId = UUID.randomUUID().toString();
        webTestClient.post().uri("/stocks")
                .syncBody(new StockCreationDto(stockId))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");
    }

    @Test
    public void testStoreDuplicatedStockForRetrievalThrowsException() {
        String stockId = UUID.randomUUID().toString();
        webTestClient.post().uri("/stocks")
                .syncBody(new StockCreationDto(stockId))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");

        webTestClient.post().uri("/stocks")
                .syncBody(new StockCreationDto(stockId))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void saveAcquistionAndDepositionReturnsCorrectStockCount() {
        String stockId = UUID.randomUUID().toString();
        StockTransactionDto stockAcquisition = new StockTransactionDto();
        stockAcquisition.setAcquisition(true);
        stockAcquisition.setPrice(100.0);
        stockAcquisition.setDate(LocalDate.now().minusDays(10));
        stockAcquisition.setCount(10);
        stockAcquisition.setStockId(stockId);
        webTestClient.post().uri("/user/transactions").syncBody(stockAcquisition)
                .exchange()
                .expectStatus().isCreated();
        webTestClient.get().uri("/user/depot")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[{stockId=" + stockId + ", count=10, averagePrice=100.0}]");

        StockTransactionDto stockDisposition = new StockTransactionDto();
        stockDisposition.setAcquisition(false);
        stockDisposition.setPrice(120.0);
        stockDisposition.setDate(LocalDate.now());
        stockDisposition.setCount(10);
        stockDisposition.setStockId(stockId);
        webTestClient.post().uri("/user/transactions").syncBody(stockDisposition)
                .exchange()
                .expectStatus().isCreated();
        webTestClient.get().uri("/user/depot")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .isEqualTo(Lists.newArrayList());

        webTestClient.post().uri("/user/transactions").syncBody(stockAcquisition)
                .exchange()
                .expectStatus().isCreated();

        stockAcquisition.setPrice(200.0);
        stockAcquisition.setDate(LocalDate.now());
        stockAcquisition.setCount(30);
        webTestClient.post().uri("/user/transactions").syncBody(stockAcquisition)
                .exchange()
                .expectStatus().isCreated();
        webTestClient
                .get().uri("/user/depot")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[{stockId=" + stockId + ", count=40, averagePrice=175.0}]");

        stockAcquisition.setStockId("APPL");
        webTestClient.post().uri("/user/transactions").syncBody(stockAcquisition)
                .exchange()
                .expectStatus().isCreated();
        stockDisposition.setPrice(200.0);
        stockDisposition.setDate(LocalDate.now());
        stockDisposition.setCount(10);
        stockDisposition.setStockId("APPL");
        webTestClient.post().uri("/user/transactions").syncBody(stockDisposition)
                .exchange()
                .expectStatus().isCreated();

        stockDisposition.setPrice(100);
        stockDisposition.setDate(LocalDate.now());
        stockDisposition.setCount(35);
        stockDisposition.setStockId(stockId);
        FluxExchangeResult<Object> response = webTestClient.post().uri("/user/transactions").syncBody(stockDisposition)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location")
                .returnResult(Object.class);

//        webTestClient.get().uri(response.getResponseHeaders().getLocation())
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("stockId").isEqualTo(STOCK_ID)
//                .jsonPath("count").isEqualTo(35)
//                .jsonPath("price").isEqualTo(100.0)
//                .jsonPath("acquisition").isEqualTo(true);

        webTestClient
                .get().uri("/user/depot")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[{stockId=" + stockId + ", count=5, averagePrice=200.0}, {stockId=APPL, count=20, averagePrice=200.0}]");
    }

}