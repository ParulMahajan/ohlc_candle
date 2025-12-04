package com.example.candle.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.candle.entity.Candle;

public interface CandleRepository extends JpaRepository<Candle, Long> {

    List<Candle> findBySymbolAndIntervalAndTimeBetweenOrderByTime(
            String symbol,
            String interval,
            Instant from,
            Instant to
    );
    
   
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO candles(symbol, interval, time, open, high, low, close, volume)
            VALUES (:symbol, :interval, :time, :open, :high, :low, :close, :volume)
            ON CONFLICT (symbol, interval, time)
            DO UPDATE SET
                open   = EXCLUDED.open,
                high   = EXCLUDED.high,
                low    = EXCLUDED.low,
                close  = EXCLUDED.close,
                volume = EXCLUDED.volume
            """, nativeQuery = true)
    void upsertCandle(String symbol,
                      String interval,
                      Instant time,
                      double open,
                      double high,
                      double low,
                      double close,
                      long volume);

}

