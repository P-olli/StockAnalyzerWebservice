package de.olli.repository;

import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.PriceBase;
import de.olli.model.dto.StockDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataMongoTest
@Slf4j
public class StocksMongoDBRepositoryIT {

    private static final String STOCK_ID = "NDX1.DE";
    private static final String STOCK_ID1 = "APPL";
    private static final String STOCK_ID2 = "AMZN";
    @Autowired
    private PriceMongoDBReactiveRepo repo;

    @Before
    public void setUp() {
        repo.deleteAll().block();
    }

    @Test
    public void testSaveStock() {
        LocalDateTime now = LocalDateTime.now();
        StockDto stockDto = new StockDto();
        stockDto.setId(STOCK_ID);
        stockDto.addPrice(new Price(STOCK_ID, now, 1d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(1), 2d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(2), 3d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(3), 4d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(4), 5d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(5), 6d));
        Flux<Price> saved = repo.saveAll(Lists.transform(stockDto.getPrices(), PriceBase::toPrice));
        saved.blockFirst();
        Flux<Price> firstThree = repo.findByStockIdOrderByDayDesc(STOCK_ID, PageRequest.of(0,3));
        List<Price> list = firstThree.collectList().block();
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0).getDay()).isEqualTo(now);

        Flux<Price> secondThree = repo.findByStockIdOrderByDayDesc(STOCK_ID,PageRequest.of(1, 3));
        List<Price> list2 = secondThree.collectList().block();
        assertThat(list2.size()).isEqualTo(3);
        assertThat(list2.get(0).getDay()).isEqualTo(now.minusDays(3));
    }

    @Test
    public void testRetrieveDistinctStocks() {
        LocalDateTime now = LocalDateTime.now();
        StockDto stockDto = new StockDto();
        stockDto.setId(STOCK_ID);
        stockDto.addPrice(new Price(STOCK_ID, now, 1d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(1), 2d));
        stockDto.addPrice(new Price(STOCK_ID, now.minusDays(2), 3d));
        Flux<Price> saved = repo.saveAll(Lists.transform(stockDto.getPrices(), PriceBase::toPrice));
        saved.blockFirst();
        StockDto stockDto1 = new StockDto();
        stockDto.setId(STOCK_ID1);
        stockDto1.addPrice(new Price(STOCK_ID1, now, 1d));
        stockDto1.addPrice(new Price(STOCK_ID1, now.minusDays(1), 2d));
        stockDto1.addPrice(new Price(STOCK_ID1, now.minusDays(2), 3d));
        Flux<Price> saved1 = repo.saveAll(Lists.transform(stockDto1.getPrices(), PriceBase::toPrice));
        saved1.blockFirst();
        StockDto stockDto2 = new StockDto();
        stockDto2.setId(STOCK_ID2);
        stockDto2.addPrice(new Price(STOCK_ID2, now, 1d));
        stockDto2.addPrice(new Price(STOCK_ID2, now.minusDays(1), 2d));
        stockDto2.addPrice(new Price(STOCK_ID2, now.minusDays(2), 3d));
        Flux<Price> saved2 = repo.saveAll(Lists.transform(stockDto2.getPrices(), PriceBase::toPrice));
        saved2.blockFirst();

        Flux<Price> all = repo.findAll();
        List<Price> list = all.collectList().block();
        Flux<Price> distinctStockIds = all.distinct(price -> price.getStockId());
        List<Price> distinctList = distinctStockIds.collectList().block();
        assertThat(distinctList.size()).isEqualTo(3);
        assertThat(distinctList.get(0).getStockId()).isEqualTo(STOCK_ID);
        assertThat(distinctList.get(1).getStockId()).isEqualTo(STOCK_ID1);
        assertThat(distinctList.get(2).getStockId()).isEqualTo(STOCK_ID2);
    }

}