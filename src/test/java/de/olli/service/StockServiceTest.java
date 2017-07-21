package de.olli.service;

import de.olli.model.Stock;
import org.assertj.core.util.Lists;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
    public void testgetStocksWithTwoStocks() throws Exception {
        List<Stock> stocks = stockService.getStocks(Lists.newArrayList("NDX1.DE", "AAPL"));
        assertThat(stocks).hasSize(2);
    }
}