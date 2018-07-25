package de.olli.service;

import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.Stock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by olli on 20.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("offline")
public class StockServiceIT {

    @Autowired
    private StockService stockService;

    @Test
    public void testgetStocksWithOneStock() throws Exception {
        Flux<Price> prices = stockService.getStock("NDX1.DE");
        assertThat(prices.count().block()).isEqualTo(4398);
    }

    @Test
    public void movingAverageCalculationReturnsAverageOfLastValues() throws Exception {
        ArrayList<Price> prices = Lists.newArrayList();
        prices.add(new Price("NDX1.DE", LocalDateTime.now(), new Double(1)));
        prices.add(new Price("NDX1.DE", LocalDateTime.now(), new Double(2)));
        prices.add(new Price("NDX1.DE", LocalDateTime.now(), new Double(3)));
        prices.add(new Price("NDX1.DE", LocalDateTime.now(), new Double(4)));
        prices.add(new Price("NDX1.DE", LocalDateTime.now(), new Double(5)));
        prices.add(new Price("NDX1.DE", LocalDateTime.now(), new Double(6)));
        Flux<Double> fluxAverages = stockService.calculateAllPossibleMovingAverages(Flux.fromIterable(prices), 2);
        List<Double> averages = fluxAverages.collectList().block();
        assertThat(averages.size()).isEqualTo(6);
        assertThat(averages.get(0)).isEqualTo(1);
        assertThat(averages.get(1)).isEqualTo(1.5);
        assertThat(averages.get(2)).isEqualTo(2.5);
        assertThat(averages.get(3)).isEqualTo(3.5);
        assertThat(averages.get(4)).isEqualTo(4.5);
        assertThat(averages.get(5)).isEqualTo(5.5);
    }

}