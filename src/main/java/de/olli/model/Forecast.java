package de.olli.model;

import lombok.Value;

@Value
public class Forecast {
    private String stockId;
    private double forecast;
    private double avgMismatch;
    private double maxMismatch;
    private int daysToForecast;
    private int[] steps;
    private int bestMatchesCount;
}
