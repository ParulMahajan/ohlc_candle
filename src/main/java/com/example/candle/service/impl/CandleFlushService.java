package com.example.candle.service.impl;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import com.example.candle.entity.Candle;
import com.example.candle.repository.CandleRepository;


import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CandleFlushService implements SmartLifecycle{

    private static final Logger log = LoggerFactory.getLogger(CandleFlushService.class);
    
    private final CandleRepository repo;
    private final BlockingQueue<Candle> flushQueue;
    private  ExecutorService executor;

    private final List<Candle> batch = new ArrayList<>(500);
    private volatile boolean running = false;

  

    @Override
    public void start() {
    	if (!running) {
            running = true;
            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::runLoop);
            log.info("CandleFlushService thread started");
        }
    }

    private void runLoop() {

    	try {

    		while (running && !Thread.currentThread().isInterrupted()) {

    			batch.clear();

    			Candle c = flushQueue.poll(50, TimeUnit.MILLISECONDS);
    			if (c == null)
    				continue;

    			batch.add(c);
    			flushQueue.drainTo(batch, 500);

    			try {
    				//repo.saveAll(batch);
    				persistBatch(batch);
    				log.debug("Saved batch of {} candles", batch.size());
    			} catch (Exception ex) {
    				log.error("Failed batch write ({} items), requeuing", batch.size(), ex);
    				for (Candle failed : batch)
    					flushQueue.offer(failed);    				
    				Thread.sleep(500);
    			}
    		}
    	} catch (InterruptedException e) {
    		Thread.currentThread().interrupt();
    		log.warn("CandleFlushService interrupted");
    	} finally {
    		log.info("CandleFlushService stopped");
    	}
    }
    
    /**
     * Persist batch using UPSERT for each candle.
     */
    private void persistBatch(List<Candle> candles) {
        for (Candle c : candles) {
            repo.upsertCandle(
                    c.getSymbol(),
                    c.getInterval(),
                    c.getTime(),
                    c.getOpen(),
                    c.getHigh(),
                    c.getLow(),
                    c.getClose(),
                    c.getVolume()
            );
        }
    }
    
    @Override
    public void stop() {
        log.warn("CandleFlushService stop() invoked — final flushing");

        running = false;  // stop loop

        // flush remaining queue
        List<Candle> batch = new ArrayList<>();
        flushQueue.drainTo(batch);
        if (!batch.isEmpty()) {
            //repo.saveAll(batch);
        	persistBatch(batch);
            log.warn("Final flush: {} candles persisted", batch.size());
        }

        executor.shutdownNow();
        log.warn("CandleFlushService stop() completed");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /** 
     * Higher phase runs LATER during shutdown
     */
    @Override
    public int getPhase() {
        return 0;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
   

    
}

