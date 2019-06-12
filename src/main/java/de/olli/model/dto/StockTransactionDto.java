package de.olli.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.olli.model.StockTransaction;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StockTransactionDto {
    private String stockId;
    private int count;
    private double price;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private boolean isAcquisition;

    public StockTransaction convert() {
        return StockTransaction.builder()
                .stockId(stockId)
                .count(count)
                .price(price)
                .date(date)
                .acquistion(isAcquisition)
                .build();
    }
}
