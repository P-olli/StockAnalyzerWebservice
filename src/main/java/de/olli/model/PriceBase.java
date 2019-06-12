package de.olli.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PriceBase implements Serializable, Comparable {

    @Id
    private String hash() {
        return String.valueOf(this.hashCode());
    }

    @Indexed
    private String stockId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Indexed
    private LocalDateTime day;
    private Double price;

    @Override
    public int compareTo(Object o) {
        Price other = (Price) o;
        return this.stockId.equals(other) ? this.day.compareTo(other.getDay()) : -1;
    }

    public Price toPrice() {
        return new Price(this);
    }

    public IntradayPrice toIntradayPrice() {
        return new IntradayPrice(this);
    }

}
