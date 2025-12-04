package com.example.candle.controller;



import com.example.candle.model.BidAskEvent;
import com.example.candle.service.CandleAggregationService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticks")
@RequiredArgsConstructor
public class TickController {

    private static final Logger log = LoggerFactory.getLogger(TickController.class);
    private final CandleAggregationService aggregationService;

    @PostMapping
    public ResponseEntity<Void> submitTick(@RequestBody BidAskEvent event) {
        log.info("Received tick via API: {}", event);
        aggregationService.onBidAskEvent(event);
        return ResponseEntity.accepted().build();
    }
}

