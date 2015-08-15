/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic.aggregation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.spotify.heroic.common.DateRange;
import com.spotify.heroic.common.Statistics;

/**
 * A special aggregation method that is a chain of other aggregation methods.
 *
 * @author udoprog
 */
@Data
@EqualsAndHashCode(of = { "NAME", "chain" })
public class ChainAggregation implements Aggregation {
    private static final ArrayList<Aggregation> EMPTY_AGGREGATORS = new ArrayList<>();
    public static final String NAME = "chain";

    private final List<Aggregation> chain;

    @JsonCreator
    public ChainAggregation(@JsonProperty("chain") List<Aggregation> chain) {
        this.chain = checkNotEmpty(Optional.fromNullable(chain).or(EMPTY_AGGREGATORS), "chain");
    }

    static <T> List<T> checkNotEmpty(List<T> list, String what) {
        if (list.isEmpty())
            throw new IllegalArgumentException(what + " must not be empty");

        return list;
    }

    /**
     * For chain aggregations, the extent is the biggest among all children.
     */
    @Override
    public long extent() {
        long extent = 0;

        for (final Aggregation a : chain) {
            extent = Math.max(a.extent(), extent);
        }

        return extent;
    }

    @Override
    public long estimate(DateRange range) {
        long best = -1;

        for (final Aggregation a : chain) {
            final long estimate = a.estimate(range);

            if (estimate != -1)
                best = estimate;
        }

        return best;
    }

    @Override
    public AggregationTraversal session(List<AggregationState> groups, DateRange range) {
        final Iterator<Aggregation> iter = chain.iterator();

        final Aggregation first = iter.next();
        final AggregationTraversal head = first.session(groups, range);

        AggregationTraversal prev = head;

        final List<AggregationSession> tail = new ArrayList<>();

        while (iter.hasNext()) {
            final AggregationTraversal s = iter.next().session(prev.getStates(), range);
            tail.add(s.getSession());
            prev = s;
        }

        return new AggregationTraversal(prev.getStates(), new Session(head.getSession(), tail));
    }

    @RequiredArgsConstructor
    private static final class Session implements AggregationSession {
        private final AggregationSession first;
        private final Iterable<AggregationSession> rest;

        @Override
        public void update(AggregationData update) {
            first.update(update);
        }

        @Override
        public AggregationResult result() {
            final AggregationResult firstResult = first.result();
            List<AggregationData> current = firstResult.getResult();
            Statistics.Aggregator statistics = firstResult.getStatistics();

            for (final AggregationSession session : rest) {
                for (AggregationData u : current)
                    session.update(u);

                final AggregationResult next = session.result();
                current = next.getResult();
                statistics = statistics.merge(next.getStatistics());
            }

            return new AggregationResult(current, statistics);
        }
    }

    public static Aggregation convertQueries(List<AggregationQuery<?>> aggregators) {
        if (aggregators == null)
            throw new NullPointerException("aggregators");

        if (aggregators.isEmpty())
            return EmptyAggregation.INSTANCE;

        if (aggregators.size() == 1)
            return aggregators.iterator().next().build();

        return new ChainAggregation(convertQueriesAsList(aggregators));
    }

    public static List<Aggregation> convertQueriesAsList(List<AggregationQuery<?>> aggregators) {
        final List<Aggregation> result = new ArrayList<>(aggregators.size());

        for (final AggregationQuery<?> aggregation : aggregators)
            result.add(aggregation.build());

        return result;
    }
}
