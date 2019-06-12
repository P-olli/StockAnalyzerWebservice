package de.olli.service;

import com.google.common.collect.Lists;
import de.olli.model.Price;
import de.olli.model.Stock;
import de.olli.repository.StockBaseMongoReactiveRepo;
import de.olli.repository.IntradayPriceMongoDBReactiveRepo;
import de.olli.repository.PriceMongoDBReactiveRepo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static de.olli.util.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StockServiceTest {

    @Mock
    private PriceMongoDBReactiveRepo mockRepo;
    @Mock
    private StockDataRetriever mockDataRetriever;
    @Mock
    private IntradayPriceMongoDBReactiveRepo mockIntradayRepo;
    @Mock
    private StockBaseMongoReactiveRepo mockStockBaseRepo;

    @InjectMocks
    private StockService stockService;

    @Before
    public void setUp() {
        when(mockDataRetriever.getStockFromApi(anyString(), eq(true))).thenReturn(Mono.just(createStock(STOCK_ID_EMPTY_DB, LocalDateTime.now())));
        when(mockStockBaseRepo.findByStockId(anyString())).thenReturn(Mono.just(Stock.builder().stockId(STOCK_ID_EMPTY_DB).build()));
        when(mockRepo.findByStockIdOrderByDayDesc(eq(STOCK_ID_EMPTY_DB), any())).thenReturn(Flux.empty());
        when(mockRepo.findByStockIdOrderByDayDesc(eq(STOCK_ID_NEWER_ENTRIES), any())).thenReturn(Flux.fromIterable(Lists.newArrayList(new Price(STOCK_ID_NEWER_ENTRIES, LocalDateTime.now().minusDays(99), 1.0))));
    }

    @Test
    public void getStocksWithNoDataInDBCallsApiFull() throws Exception {
        Flux<Price> prices = stockService.getStock(STOCK_ID_EMPTY_DB, PageRequest.of(0, 2));
        verify(mockDataRetriever, times(1)).getStockFromApi(STOCK_ID_EMPTY_DB, true);
        assertThat(prices.count().block()).isEqualTo(1);
    }

    @Test
    public void getStocksWithDataInDBNewerThan100DaysCallsApiUnfull() throws Exception {
        Flux<Price> prices = stockService.getStock(STOCK_ID_NEWER_ENTRIES, PageRequest.of(0, 2));
        verifyZeroInteractions(mockDataRetriever);
        assertThat(prices.count().block()).isEqualTo(1);
    }

    @Test
    @Ignore
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