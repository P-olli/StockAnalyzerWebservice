package de.olli.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.olli.model.IntradayPrice;

import java.time.LocalDateTime;
import java.util.Map;

public class PriceDto {
    private String stockId;
    private LocalDateTime day;
    private Double price;

    @JsonProperty("Global Quote")
    private void setPrice(Map<String, String> price) {
        this.stockId = price.get("01. symbol");
        this.day = LocalDateTime.now();
        this.price = Double.parseDouble(price.get("05. price"));
    }

    public IntradayPrice toIntradayPrice() {
        IntradayPrice intradayPrice = new IntradayPrice();
        intradayPrice.setStockId(this.stockId);
        intradayPrice.setDay(this.day);
        intradayPrice.setPrice(this.price);
        return intradayPrice;
    }
}
