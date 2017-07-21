package de.olli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by olli on 12.04.2017.
 */
public class Stock {

    private String id;
    private Double movingAverage38;
    private Double movingAverage100;
    private List<Price> prices = new ArrayList<>();
    @JsonIgnore
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @JsonProperty("Meta Data")
    private void setId(Map<String, String> metadata) {
        this.id = metadata.get("2. Symbol");
    }

    @JsonProperty("Time Series (Daily)")
    private void setPrices(Map<String, Map<String, String>> prices) {
        prices.entrySet().forEach(entry -> {
            DateTime date;
            if(entry.getKey().contains(" ")) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                date = dateTimeFormatter.parseDateTime(entry.getKey());
            } else {
                date = DateTime.parse(entry.getKey());
            }
            this.prices.add(new Price(date.toDate(), Double.parseDouble(entry.getValue().get("4. close"))));
        });
    }

    @JsonProperty("Id")
    public String getId() {
        return id;
    }

    @JsonProperty("Prices")
    public List<Price> getPrices() {
        return this.prices;
    }

    @JsonProperty("MovingAverage38")
    public Double getMovingAverage38() {
        return movingAverage38;
    }

    @JsonIgnore
    public void setMovingAverage38(Double movingAverage38) {
        this.movingAverage38 = movingAverage38;
    }

    @JsonProperty("MovingAverage100")
    public Double getMovingAverage100() {
        return movingAverage100;
    }

    @JsonIgnore
    public void setMovingAverage100(Double movingAverage100) {
        this.movingAverage100 = movingAverage100;
    }
}
