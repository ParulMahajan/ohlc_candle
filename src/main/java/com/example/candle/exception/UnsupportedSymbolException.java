package com.example.candle.exception;

public class UnsupportedSymbolException extends RuntimeException {
    public UnsupportedSymbolException(String symbol) {
        super("Symbol '" + symbol + "' is not supported");
    }
}