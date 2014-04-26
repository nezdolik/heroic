package com.spotify.heroic.model;

import lombok.Getter;

import com.spotify.heroic.aggregator.Aggregation;

public class CacheKey {
    /**
     * Includes key and tags.
     */
    @Getter
    private final TimeSerie timeSerie;

    /**
     * Always includes sampling.
     */
    @Getter
    private final Aggregation aggregation;

    @Getter
    private final long base;

    public CacheKey(TimeSerie timeSerie, Aggregation aggregation, long base) {
        this.timeSerie = timeSerie;
        this.aggregation = aggregation;
        this.base = base;
    }
}