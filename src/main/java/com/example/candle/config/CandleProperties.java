package com.example.candle.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.example.candle.model.Interval;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "candle")
@Data
public class CandleProperties {
    
    private TickGeneration tickGeneration = new TickGeneration();
    private Map<String, SymbolConfig> symbols = new HashMap<>();
    private List<String> supportedIntervals = new ArrayList<>();
    private int recentCandlesCacheLimit;
    
    public Set<String> getSupportedSymbols() {
        return symbols.keySet();
    }
    
    public List<Interval> getSupportedIntervalEnums() {
        return supportedIntervals.stream()
            .map(Interval::valueOf)
            .toList();
    }
    
    @Data
    public static class TickGeneration {
        private boolean enabled = true;
        private int rateMillis;
    }
    
    @Data
    public static class SymbolConfig {
        private PriceBounds bounds;
        private double spread;
        private double volatility;
    }
    
    @Data
    public static class PriceBounds {
        private double min;
        private double max;
        private double initial;
        
        public double clamp(double price) {
            return Math.max(min, Math.min(max, price));
        }
    }
}
