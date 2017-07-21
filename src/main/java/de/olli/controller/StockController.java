package de.olli.controller;

import de.olli.model.Stock;
import de.olli.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by olli on 18.04.2017.
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @RequestMapping("/stocks/{stockNames:.+}")
    public List<Stock> getStocks(@PathVariable List<String> stockNames) {
        return stockService.getStocks(stockNames);
    }
}
