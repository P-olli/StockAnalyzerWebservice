package de.olli.service.batch;

import de.olli.model.dto.PriceDto;
import de.olli.model.Stock;
import de.olli.repository.IntradayPriceMongoDBReactiveRepo;
import de.olli.repository.PriceMongoDBReactiveRepo;
import de.olli.repository.StockBaseMongoReactiveRepo;
import de.olli.service.StockDataRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobDaily {

    private final IntradayPriceMongoDBReactiveRepo intradayRepo;
    private final StockBaseMongoReactiveRepo stockBaseRepo;
    private final PriceMongoDBReactiveRepo priceMongoDBReactiveRepo;
    private final StockDataRetriever stockDataRetriever;

   // @Scheduled(cron = "0 * * * * MON-FRI")
    public void retrieveLastValueAndStoreInDB() {
        log.info("Triggered intraday retrieval!");
        Flux<Stock> allStocks = stockBaseRepo.findAll();
        allStocks.collectList().block().forEach(stock -> log.info(stock.getStockId()));
        allStocks.toStream().parallel().forEach(stock -> {
            log.info("Got data for {}.", stock.getStockId());
            Optional<PriceDto> latestPrice = stockDataRetriever.getLatestPriceFromApi(stock.getStockId()).blockOptional();
            if(latestPrice.isPresent()) {
                intradayRepo.save(latestPrice.get().toIntradayPrice()).block();
            }
        });
        log.info("Ended intraday retrieval.");
    }

    //@Scheduled(cron = "0 30 22 * * MON-FRI")
    public void transferFinalQuotationDaily() {
        log.info("Triggered quotation transfer!");
        Flux<Stock> allStocks = stockBaseRepo.findAll();
        allStocks.collectList().block().forEach(stock -> log.info(stock.getStockId()));
        allStocks.toStream().forEach(stock -> {
            priceMongoDBReactiveRepo.save(intradayRepo.findFirstByStockIdAndDay(stock.getStockId()).block());
        });
        log.info("Ended quotation transfer.");
    }
}
