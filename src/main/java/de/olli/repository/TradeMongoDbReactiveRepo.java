package de.olli.repository;

import de.olli.model.StockTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TradeMongoDbReactiveRepo extends ReactiveMongoRepository<StockTransaction, String> {

    Mono<StockTransaction> findById(String transactionId);

}
