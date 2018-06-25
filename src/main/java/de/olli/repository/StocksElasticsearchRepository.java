package de.olli.repository;

import de.olli.model.Price;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface StocksElasticsearchRepository extends ElasticsearchRepository<Price, String> {

    Stream<Price> streamPriceByStockId(String stockId);

    long countAllByStockId(String stockId);

    Price getDistinctFirstByDayAndPrice(String stockId);

}
