package de.olli.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@Document
public class StockTransaction implements Comparable{
    @Id
    private String id;
    private String stockId;
    private int count;
    private double price;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private boolean acquistion;

    @Override
    public int compareTo(Object o) {
        int result = stockId.compareTo(((StockTransaction) o).getStockId());
        return result != 0 ? result : this.date.compareTo(((StockTransaction)o).getDate());
    }
}
