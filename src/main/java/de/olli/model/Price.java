package de.olli.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by olli on 03.07.2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document
//@CompoundIndexes({
//        @CompoundIndex(name = "price_unique",
//                unique = true,
//                def = "{'stockId' : 1, 'day' : 1}")
//})
public class Price implements Serializable, Comparable {

    @Id
    private String hash() {
        return String.valueOf(this.hashCode());
    }

    private String stockId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime day;
    private Double price;

    @Override
    public int compareTo(Object o) {
        Price other = (Price) o;
        return this.stockId.equals(other) ? this.day.compareTo(other.day) : -1;
    }
}
