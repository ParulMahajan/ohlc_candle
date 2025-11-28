package com.example.candle.model;

import java.time.Duration;

import com.example.candle.exception.UnsupportedIntervalException;

public enum Interval {
    SEC_1(Duration.ofSeconds(1)),
    SEC_5(Duration.ofSeconds(5)),
    MIN_1(Duration.ofMinutes(1)),
    MIN_15(Duration.ofMinutes(15)),
    MIN_30(Duration.ofMinutes(30)),
    HOUR_1(Duration.ofHours(1)),
    DAY_1(Duration.ofDays(1));

    private final Duration duration;

    Interval(Duration duration) {
    	this.duration = duration; 
    	}

    public Duration getDuration() { 
    	return duration; 
    	}
    
    public String toWireString() {
        return switch (this) {
            case SEC_1 -> "1s";
            case SEC_5 -> "5s";
            case MIN_1 -> "1m";
            case MIN_15 -> "15m";
            case MIN_30 -> "30m";
            case HOUR_1 -> "1h";
            case DAY_1 -> "1d";
        };
    }

    public static Interval fromWireString(String s) {
        return switch (s) {
            case "1s" -> SEC_1;
            case "5s" -> SEC_5;
            case "1m" -> MIN_1;
            case "15m" -> MIN_15;
            case "30m" -> MIN_30;
            case "1h" -> HOUR_1;
            case "1d" -> DAY_1;
            default -> throw new UnsupportedIntervalException(s);
        };
    }
}

