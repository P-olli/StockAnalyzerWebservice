package de.olli.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@Builder
public class Stock {

    @Id
    private String id;
    @Indexed(unique = true)
    private final String stockId;
    private double weight1;
    private double weight2;
    private double weight3;
    private double weight4;
    private double bestMatchesCount;
}
