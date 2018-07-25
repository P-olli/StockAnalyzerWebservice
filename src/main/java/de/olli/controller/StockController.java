package de.olli.controller;

import de.olli.model.Price;
import de.olli.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Created by olli on 18.04.2017.
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/stocks/{stockId}")
    public Flux<Price> getStocks(@PathVariable String stockId) {
        return stockService.getStock(stockId);
    }
}
