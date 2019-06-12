package de.olli.controller;

import de.olli.model.*;
import de.olli.model.dto.StockCreationDto;
import de.olli.model.dto.StockTransactionDto;
import de.olli.service.AnalyzingService;
import de.olli.service.StockService;
import de.olli.service.UserStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * Created by olli on 18.04.2017.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {

    private final StockService stockService;
    private final UserStockService userStockService;
    private final AnalyzingService analyzingService;

    @PostMapping("/stocks")
    public ResponseEntity storeStockIdForRetrieval(@RequestBody StockCreationDto stock) {
        stockService.storeStockForRetrieval(stock);
        return ResponseEntity.created(URI.create("/stocks/" + stock.getStockId())).build();
    }

    @DeleteMapping("/stocks/{stockId}")
    public ResponseEntity deleteStockIdForRetrieval(@PathVariable String stockId) {
        stockService.deleteStockForRetrieval(stockId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stocks")
    public Flux<Stock> getAllStocks() {
        return stockService.getAllStocks();
    }

    @GetMapping("/stocks/{stockId:.+}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Flux<Price>> getStocks(@PathVariable String stockId, @RequestParam TimeRange timeRange, @RequestParam int page, @RequestParam int size) {
        if(timeRange == TimeRange.daily) {
            Flux<Price> stock = stockService.getStock(stockId, PageRequest.of(page, size));
            return ResponseEntity.ok().body(stock);
        } else {
            Flux<Price> stockDaily = stockService.getStockDaily(stockId);
            return ResponseEntity.ok().body(stockDaily);
        }
    }

    @GetMapping("/stocks/{stockId}/rating")
    public Mono<Double> getStockRating(@PathVariable String stockId) {
        return Mono.just(analyzingService.calculateStockRating(stockId));
    }

    @GetMapping("/stocks/{stockId}/forecast")
    public Flux<Forecast> getStockForecast(@PathVariable String stockId, @RequestParam int daysToForecast) {
        return Flux.fromIterable(analyzingService.calculateForecasts(stockId, daysToForecast));
    }

    @GetMapping("/user/depot")
    public ResponseEntity<List<DepotEntry>> getDepot() {
        List<DepotEntry> depotEntries = userStockService.getAllCurrentBoughtStocks();
        return ResponseEntity.ok().body(depotEntries);

    }

    @PostMapping("/user/transactions")
    public ResponseEntity saveTransaction(@RequestBody @Valid StockTransactionDto stockTransaction) {
        Mono<StockTransaction> savedStockTransaction = userStockService.saveTrade(stockTransaction);
        return ResponseEntity.created(URI.create("/user/transactions/" + savedStockTransaction.block().getId())).build();
    }

    @GetMapping("user/transactions")
    public ResponseEntity<Flux<StockTransaction>> getAllTransactions() {
        Flux<StockTransaction> transactions = userStockService.getAllStockTransactions();
        return ResponseEntity.ok().body(transactions);
    }

    @GetMapping("user/transactions/{transactionId}")
    public Mono<StockTransaction> getStockTransaction(@PathVariable String transactionId) {
        return userStockService.getStockTransaction(transactionId);
    }
}
