package de.olli.repository;

import de.olli.model.Price;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface StocksMongoDBReactiveRepository extends ReactiveMongoRepository<Price, String> {

    Flux<Price> findAllByStockIdOrderByDayDesc(String stockId);

}
