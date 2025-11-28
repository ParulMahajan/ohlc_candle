package com.example.candle.controller;



import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.candle.config.CandleProperties;
import com.example.candle.exception.UnsupportedIntervalException;
import com.example.candle.exception.UnsupportedSymbolException;
import com.example.candle.model.HistoryResponse;
import com.example.candle.model.Interval;
import com.example.candle.repository.CandleRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final CandleRepository repo;
    private final CandleProperties candleProperties;

    @GetMapping("/history")
    public HistoryResponse getHistory(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam long from,
            @RequestParam long to
    ) {
        if (!candleProperties.getSupportedSymbols().contains(symbol)) {
            throw new UnsupportedSymbolException(symbol);
        }
        
        Interval iv = Interval.fromWireString(interval);
        if (!candleProperties.getSupportedIntervalEnums().contains(iv)) {
            throw new UnsupportedIntervalException(interval);
        }

        Instant fromInstant = Instant.ofEpochSecond(from);
        Instant toInstant = Instant.ofEpochSecond(to);

        var candles = repo.findBySymbolAndIntervalAndTimeBetweenOrderByTime(
                symbol,
                iv.toWireString(),
                fromInstant,
                toInstant
        );

        return HistoryResponse.okFromEntities(candles);
    }
    
    @Autowired
    private ApplicationContext ctx;

    @PostMapping("/shutdown")
    public void shutdown() {
        ((ConfigurableApplicationContext) ctx).close();
    }
}

