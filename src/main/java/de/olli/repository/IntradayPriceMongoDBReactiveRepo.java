package de.olli.repository;

import de.olli.model.IntradayPrice;
import de.olli.model.Price;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IntradayPriceMongoDBReactiveRepo extends ReactiveMongoRepository<IntradayPrice, String> {

    Flux<Price> findAllByStockIdOrderByDayDesc(String stockId);

    Mono<Price> findFirstByStockIdAndDay(String stockId);

}
