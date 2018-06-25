package de.olli.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by olli on 03.07.2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "stock", type = "price")
public class Price implements Serializable, Comparable {

    @Id
    private final String uuid = UUID.randomUUID().toString();

    private String stockId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime day;
    private Double price;

    @Override
    public int compareTo(Object o) {
        return this.hashCode() - o.hashCode();
    }
}
