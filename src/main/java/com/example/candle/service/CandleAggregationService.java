package com.example.candle.service;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import com.example.candle.config.CandleProperties;
import com.example.candle.entity.Candle;
import com.example.candle.model.BidAskEvent;
import com.example.candle.model.Interval;
import com.example.candle.model.LiveCandle;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CandleAggregationService implements SmartLifecycle{

	private static final Logger log = LoggerFactory.getLogger(CandleAggregationService.class);
	private volatile boolean running = false;

	//                  symbol -> interval -> current live candle
	private final Map<String, Map<Interval, LiveCandle>> liveCandles = new ConcurrentHashMap<>(100); // Assuming hundred symbols initially
	private final Map<String, Map<Interval, List<LiveCandle>>> recentClosedCandles = new ConcurrentHashMap<>(100);  // Assuming hundred symbols initially

	private final BlockingQueue<Candle> flushQueue;

	private final CandleProperties candleProperties;

	public void onBidAskEvent(BidAskEvent event) {

		long ts = event.timestampMillis();
		String symbol = event.symbol();

		double price = (event.bid() + event.ask()) / 2.0; // midprice

		Map<Interval, LiveCandle> symbolMap = liveCandles.computeIfAbsent(symbol, s -> new ConcurrentHashMap<>());
		Map<Interval, List<LiveCandle>> closedMap = recentClosedCandles.computeIfAbsent(symbol, s -> new ConcurrentHashMap<>());

		for (Interval interval : candleProperties.getSupportedIntervalEnums()) {

			long bucketStart = BucketCalculator.bucketStartMillis(ts, interval);

			// 1) Try live candle
			LiveCandle existing = symbolMap.get(interval);
			if (existing != null) {
				if (existing.getBucketStartEpochMillis() == bucketStart) {
					existing.updateWithTick(price);
					continue; // done
				}
			}

			// 2) Try recently closed
			List<LiveCandle> closedList = closedMap.computeIfAbsent(interval, i -> new CopyOnWriteArrayList<>()); // am considering late tickers will be very less

			boolean recovered = false;

			for (LiveCandle old : closedList) {
				if (old.getBucketStartEpochMillis() == bucketStart) {
					log.info("Updated old event: {} in candle: {}",event,old);
					old.updateWithTick(price);
					// Push to DB update queue
					flushQueue.offer(toEntity(old));
					recovered = true;
					break;
				}

			}
			if (recovered) {
				continue;
			}

			// 3) This tick starts new candle
			if (existing != null) {
				// move from live → recent closed
				closedList.add(existing);
				trimClosedCache(closedList);
				flushQueue.offer(toEntity(existing)); // push to Blocking queue
			}

			// create new live candle
			symbolMap.put(interval,
					LiveCandle.newFromTick(symbol, interval, bucketStart, price));
		}
	}

	private void trimClosedCache(List<LiveCandle> closedList) {
		// limit cache size
		while (closedList.size() > candleProperties.getRecentCandlesCacheLimit()) {
			closedList.remove(0);  // remove oldest
		}
	}

	private Candle toEntity(LiveCandle live) {
		Instant start = Instant.ofEpochMilli(live.getBucketStartEpochMillis());
		return new Candle(
				live.getSymbol(),
				live.getInterval().toWireString(),
				start,
				live.getOpen(),
				live.getHigh(),
				live.getLow(),
				live.getClose(),
				live.getVolume()
				);
	}


	@Override
	public void start() {
		running = true;
		log.info("CandleAggregationService started");
	}

	@Override
	public void stop() {
		log.warn("CandleAggregationService stop() invoked — flushing LIVE candles");

		// Flush all live candles into queue for persistence
		for (var symbolMap : liveCandles.values()) {
			for (var candle : symbolMap.values()) {
				flushQueue.offer(toEntity(candle));
			}
		}

		running = false;
		log.warn("CandleAggregationService stop() completed");
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	/** 
	 * Lower phase runs first during shutdown 
	 */
	@Override
	public int getPhase() {
		return 10;   
	}
}

