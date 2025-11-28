package com.example.candle.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.candle.entity.Candle;

public interface CandleRepository extends JpaRepository<Candle, Long> {

    List<Candle> findBySymbolAndIntervalAndTimeBetweenOrderByTime(
            String symbol,
            String interval,
            Instant from,
            Instant to
    );

}

