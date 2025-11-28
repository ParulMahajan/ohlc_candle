package com.example.candle.model;

import lombok.Data;

@Data
public class LiveCandle {

    private final String symbol;
    private final Interval interval;
    private final long bucketStartEpochMillis;

    private double open;
    private double high;
    private double low;
    private double close;
    private long volume; // (number of ticks) or event-derived

    public LiveCandle(String symbol,
                      Interval interval,
                      long bucketStartEpochMillis,
                      double price) {
        this.symbol = symbol;
        this.interval = interval;
        this.bucketStartEpochMillis = bucketStartEpochMillis;
        this.open = price;
        this.high = price;
        this.low = price;
        this.close = price;
        this.volume = 1L;
    }

    public static LiveCandle newFromTick(String symbol,
                                         Interval interval,
                                         long bucketStartEpochMillis,
                                         double price) {
        return new LiveCandle(symbol, interval, bucketStartEpochMillis, price);
    }

    public void updateWithTick(double price) {
        if (price > high) high = price;
        if (price < low) low = price;
        close = price;
        volume++;
    }

}
