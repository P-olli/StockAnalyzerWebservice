package de.olli.controller;

import de.olli.model.Stock;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by olli on 21.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Ignore
public class StockControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testGetStocksLocal() throws Exception {
        Stock[] stocks = testRestTemplate.getForObject("/stocks/NDX1.DE,AAPL", Stock[].class);
        assertThat(stocks).hasSize(2);
    }
}