package com.example.candle.entity;


import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candles")
@IdClass(Candle.CandleKey.class)
@Data
@NoArgsConstructor
public class Candle {

	@Id
    private String symbol;

	@Id
    private String interval;

	@Id
    private Instant time;

    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    
    public Candle(String symbol,
    		String interval,
    		Instant time,
    		double open,
    		double high,
    		double low,
    		double close,
    		long volume) {
    	this.symbol = symbol;
    	this.interval = interval;
    	this.time = time;
    	this.open = open;
    	this.high = high;
    	this.low = low;
    	this.close = close;
    	this.volume = volume;
    }
    
    
   
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandleKey implements Serializable {
        private static final long serialVersionUID = -1483441799986213445L;
		private String symbol;
        private String interval;
        private Instant time;
    }

}
