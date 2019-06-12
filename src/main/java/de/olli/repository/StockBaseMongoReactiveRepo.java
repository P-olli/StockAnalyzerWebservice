package de.olli.repository;

import de.olli.model.Stock;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface StockBaseMongoReactiveRepo extends ReactiveMongoRepository<Stock, String> {

    Mono<Stock> findByStockId(String stockId);

}
