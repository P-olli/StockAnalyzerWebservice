package de.olli.util;

import de.olli.model.PriceBase;
import de.olli.model.dto.StockDto;

import java.time.LocalDateTime;

public class TestUtils {

    public static final String STOCK_ID_EMPTY_DB = "EMPTY_DB";
    public static final String STOCK_ID_NEWER_ENTRIES = "NEWER_ENTRIES";

    public static StockDto createStock(String stockId, LocalDateTime date) {
        PriceBase price = new PriceBase(stockId, date, 1.0);
        StockDto stockDto = new StockDto();
        stockDto.setId(stockId);
        stockDto.addPrice(price);
        return stockDto;
    }
}
