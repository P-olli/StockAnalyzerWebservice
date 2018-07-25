package de.olli.repository;

import de.olli.model.Price;
import de.olli.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataMongoTest
@Slf4j
public class StocksMongoDBRepositoryIT {

    private static final String STOCK_ID = "NDX1.DE";
    @Autowired
    private StocksMongoDBReactiveRepository repo;

    @Before
    public void setUp() {
        repo.deleteAll();
    }

    @Test
    public void testSaveStock() {
        LocalDateTime now = LocalDateTime.now();
        Stock stock = new Stock();
        stock.setId(STOCK_ID);
        stock.addPrice(new Price(STOCK_ID, now, 1d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(1), 2d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(2), 3d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(3), 4d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(4), 5d));
        stock.addPrice(new Price(STOCK_ID, now.minusDays(5), 6d));
        Flux<Price> saved = repo.saveAll(stock.getPrices());
        saved.blockFirst();
        Flux<Price> allByStockIdOOrderByDay = repo.findAllByStockIdOrderByDayDesc(STOCK_ID);
        assertThat(allByStockIdOOrderByDay.count().block()).isEqualTo(6);
        assertThat(allByStockIdOOrderByDay.blockFirst().getDay()).isEqualTo(now);
    }

}