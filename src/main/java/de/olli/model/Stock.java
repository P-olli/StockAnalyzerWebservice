package de.olli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by olli on 12.04.2017.
 */
public class Stock {

    private String id;
    private List<Double> movingAverage38;
    private List<Double> movingAverage100;
    private List<Double> movingAverage200;
    private List<Price> prices = new LinkedList<>();

    @JsonProperty("Meta Data")
    private void setId(Map<String, String> metadata) {
        this.id = metadata.get("2. Symbol");
    }

    @JsonProperty("Time Series (Daily)")
    private void setPrices(Map<String, Map<String, String>> prices) {
        prices.entrySet().forEach(entry -> {
            LocalDateTime date;
            date = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(entry.getKey()));
            this.prices.add(new Price(entry.getKey(), date, Double.parseDouble(entry.getValue().get("4. close"))));
        });
    }

    @JsonProperty("Id")
    public String getId() {
        return id;
    }

    @JsonIgnore
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("Prices")
    public List<Price> getPrices() {
        return this.prices;
    }

    @JsonIgnore
    public void addPrice(Price price) {
        this.prices.add(price);
    }

    @JsonProperty("MovingAverage38")
    public List<Double> getMovingAverage38() {
        return movingAverage38;
    }

    @JsonIgnore
    public void setMovingAverage38(List<Double> movingAverage38) {
        this.movingAverage38 = movingAverage38;
    }

    @JsonProperty("MovingAverage100")
    public List<Double> getMovingAverage100() {
        return movingAverage100;
    }

    @JsonIgnore
    public void setMovingAverage100(List<Double> movingAverage100) {
        this.movingAverage100 = movingAverage100;
    }

    @JsonProperty("MovingAverage200")
    public List<Double> getMovingAverage200() {
        return movingAverage200;
    }

    @JsonIgnore
    public void setMovingAverage200(List<Double> movingAverage200) {
        this.movingAverage200 = movingAverage200;
    }
}
