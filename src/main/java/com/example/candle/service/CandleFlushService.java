package com.example.candle.service;



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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CandleFlushService implements SmartLifecycle{

    private static final Logger log = LoggerFactory.getLogger(CandleFlushService.class);
    
    private final CandleRepository repo;
    private final BlockingQueue<Candle> flushQueue;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile boolean running = true;

  

    @PostConstruct
    public void start() {
        executor.submit(this::runLoop);
    }

    private void runLoop() {
        log.info("CandleFlushService started");
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
            
            	  List<Candle> batch = new ArrayList<>();

                  // wait for 1 item up to 100ms
                  Candle first = flushQueue.poll(100, TimeUnit.MILLISECONDS);
                  if (first != null) {
                      batch.add(first);
                      flushQueue.drainTo(batch, 200);
                  }

                  if (!batch.isEmpty()) {
                      repo.saveAll(batch);
                      repo.flush();
                      log.info("saved to DB, size:{}", batch.size());
                  }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("CandleFlushService interrupted");
        } finally {
            log.info("CandleFlushService stopped");
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
            repo.saveAll(batch);
            repo.flush();
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
   

    
}

