package de.olli.repository;

import de.olli.model.Price;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PriceMongoDBReactiveRepo extends ReactiveMongoRepository<Price, String> {

    Flux<Price> findByStockIdOrderByDayDesc(String stockId, Pageable pageable);

}
