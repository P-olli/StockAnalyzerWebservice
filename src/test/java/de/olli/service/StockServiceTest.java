package de.olli.service;

import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.Stock;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by olli on 20.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("offline")
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Test
    public void testgetStocksWithOneStock() throws Exception {
        List<Stock> stocks = stockService.getStocks(Lists.newArrayList("NDX1.DE"));
        assertThat(stocks).hasSize(1);
    }

    @Test
    public void movingAverageCalculationReturnsAverageOfLastValues() throws Exception {
        ArrayList<Price> prices = Lists.newArrayList();
        prices.add(new Price(DateTime.now().toDate(), new Double(1)));
        prices.add(new Price(DateTime.now().toDate(), new Double(2)));
        prices.add(new Price(DateTime.now().toDate(), new Double(3)));
        prices.add(new Price(DateTime.now().toDate(), new Double(4)));
        prices.add(new Price(DateTime.now().toDate(), new Double(5)));
        prices.add(new Price(DateTime.now().toDate(), new Double(6)));
        List<Double> average = stockService.calculateAllPossibleMovingAverages(prices, 2);
        assertThat(average).hasSize(6);
        assertThat(average.get(0)).isEqualTo(1);
        assertThat(average.get(1)).isEqualTo(1.5);
        assertThat(average.get(2)).isEqualTo(2.5);
        assertThat(average.get(3)).isEqualTo(3.5);
        assertThat(average.get(4)).isEqualTo(4.5);
        assertThat(average.get(5)).isEqualTo(5.5);
    }

}