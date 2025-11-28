package com.example.candle.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.candle.model.HistoryResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedSymbolException.class)
    public ResponseEntity<HistoryResponse> handleUnsupportedSymbol(UnsupportedSymbolException ex) {
        return ResponseEntity.badRequest().body(HistoryResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedIntervalException.class)
    public ResponseEntity<HistoryResponse> handleUnsupportedInterval(UnsupportedIntervalException ex) {
        return ResponseEntity.badRequest().body(HistoryResponse.error(ex.getMessage()));
    }
}