package de.olli.repository;

import de.olli.model.Price;
import de.olli.model.Stock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StocksElasticSearchRepositoryIT {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private StocksElasticsearchRepository repo;

    @Before
    public void setup() {
        elasticsearchTemplate.deleteIndex(Price.class);
        elasticsearchTemplate.createIndex(Price.class);
        elasticsearchTemplate.putMapping(Price.class);
        elasticsearchTemplate.refresh(Price.class);
    }

    @Test
    public void testSavePrice() {
        Price price = new Price("NDX1.DE", LocalDateTime.now(), 10d);
        Price savedPrice = repo.save(price);

        assertNotNull(savedPrice);

        long count = repo.countAllByStockId("NDX1.DE");
        assertEquals(count, 1);

        Price priceRepo = repo.streamPriceByStockId("NDX1.DE").findFirst().get();
        assertThat(priceRepo).isEqualTo(price);
    }

    @Test
    public void testSaveStock() {
        Stock stock = new Stock();
        stock.setId("NDX1.DE");
        stock.addPrice(new Price("NDX1.DE", LocalDateTime.now(), new Double(1)));
        stock.addPrice(new Price("NDX1.DE", LocalDateTime.now().minusDays(1), new Double(2)));
        stock.addPrice(new Price("NDX1.DE", LocalDateTime.now().minusDays(2), new Double(3)));
        stock.addPrice(new Price("NDX1.DE", LocalDateTime.now().minusDays(3), new Double(4)));
        stock.addPrice(new Price("NDX1.DE", LocalDateTime.now().minusDays(4), new Double(5)));
        stock.addPrice(new Price("NDX1.DE", LocalDateTime.now().minusDays(5), new Double(6)));
        repo.saveAll(stock.getPrices());
        assertThat(repo.streamPriceByStockId("NDX1.DE").count()).isEqualTo(6);
    }
}