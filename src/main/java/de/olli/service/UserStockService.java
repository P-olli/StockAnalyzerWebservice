package de.olli.service;

import com.google.common.collect.Lists;
import de.olli.model.DepotEntry;
import de.olli.model.StockTransaction;
import de.olli.model.dto.StockTransactionDto;
import de.olli.repository.TradeMongoDbReactiveRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStockService {
    private final TradeMongoDbReactiveRepo acquistionRepo;

    public Mono<StockTransaction> saveTrade(StockTransactionDto stockTransaction) {
        return acquistionRepo.save(stockTransaction.convert());
    }

    public Flux<StockTransaction> getAllStockTransactions() {
        return acquistionRepo.findAll(Sort.by("stockId", "date"));
    }

    public Mono<StockTransaction> getStockTransaction(String transactionId) {
        return acquistionRepo.findById(transactionId);
    }

    public List<DepotEntry> getAllCurrentBoughtStocks()  {
        Flux<StockTransaction> allStockTransactions = getAllStockTransactions();
        List<DepotEntry> depotEntries = Lists.newArrayList();
        allStockTransactions.toStream().sorted().forEach(t -> {
            if(depotEntries.size() > 0 && depotEntries.get(depotEntries.size() - 1).getStockId().equals(t.getStockId())) {
                depotEntries.get(depotEntries.size() -1).addTransaction(t);
            } else {
                depotEntries.add(DepotEntry.createDepot(t));
            }
        });
        return depotEntries.stream().filter(t -> t.getCount() != 0).collect(Collectors.toList());
    }

}
