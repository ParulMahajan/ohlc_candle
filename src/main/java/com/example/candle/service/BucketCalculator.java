package com.example.candle.service;

import com.example.candle.model.Interval;

public final class BucketCalculator {

    private BucketCalculator() {
    }

    public static long bucketStartMillis(long timestampMillis, Interval interval) {
        long sizeMs = interval.getDuration().toMillis();
        return (timestampMillis / sizeMs) * sizeMs;
    }
}

