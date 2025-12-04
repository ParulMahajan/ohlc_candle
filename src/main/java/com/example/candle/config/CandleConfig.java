package com.example.candle.config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.candle.entity.Candle;

@Configuration
public class CandleConfig {

	@Bean
	public BlockingQueue<Candle> flushQueue() {
		// Unbounded for demo; in prod i will bound it to 100-100
		return new LinkedBlockingQueue<>();
	}
}
