package com.example.candle.exception;

public class UnsupportedIntervalException extends RuntimeException {
    public UnsupportedIntervalException(String interval) {
        super("Interval '" + interval + "' is not supported");
    }
}