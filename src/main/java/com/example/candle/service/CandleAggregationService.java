package com.example.candle.service;

import com.example.candle.model.BidAskEvent;

public interface CandleAggregationService {

	public void onBidAskEvent(BidAskEvent event);
}
