package com.example.candle.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.candle.config.CandleProperties;
import com.example.candle.model.BidAskEvent;
import com.example.candle.service.CandleAggregationService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "candle.tick-generation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RandomTickGenerator implements SmartLifecycle{

    private static final Logger log = LoggerFactory.getLogger(RandomTickGenerator.class);
    private volatile boolean running = false;
    
    private final CandleAggregationService aggregationService;
    private final CandleProperties properties;
    private final Random random = new Random();
    
    // Persistent price state - symbol -> current price
    private final Map<String, Double> currentPrices = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Initialize prices from configuration
        properties.getSymbols().forEach((symbol, config) -> {
            currentPrices.put(symbol, config.getBounds().getInitial());
            log.info("Initialized {} at price {}", symbol, config.getBounds().getInitial());
        });
    }
 

    @Scheduled(fixedRateString = "${candle.tick-generation.rate-millis:500}")
    public void generateTicks() {
        long now = System.currentTimeMillis();
        
        properties.getSymbols().forEach((symbol, config) -> {
            double currentPrice = currentPrices.get(symbol);
            
            // Apply random walk with volatility
            currentPrice += random.nextGaussian() * config.getVolatility();
            
            // Clamp to bounds
            currentPrice = config.getBounds().clamp(currentPrice);
            
            // Update persistent state
            currentPrices.put(symbol, currentPrice);
            
            // Generate bid/ask with spread
            double halfSpread = config.getSpread() / 2.0;
            double bid = currentPrice - halfSpread;
            double ask = currentPrice + halfSpread;
            
            BidAskEvent event = new BidAskEvent(symbol, bid, ask, now);
            log.info("Sending tick: {}", event);
            aggregationService.onBidAskEvent(event);
        });
    }
    
    @Override
    public void start() {
        running = true;
        log.info("RandomTickGenerator started");
    }

    @Override
    public void stop() {
        log.warn("RandomTickGenerator stopped");
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 100;  // Must stop BEFORE aggregation
    }
    
   
}