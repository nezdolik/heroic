package com.spotify.heroic.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.spotify.heroic.aggregation.Aggregation;
import com.spotify.heroic.cluster.ClusterNode.Group;
import com.spotify.heroic.common.DateRange;
import com.spotify.heroic.common.RangeFilter;
import com.spotify.heroic.common.Series;
import com.spotify.heroic.filter.Filter;
import com.spotify.heroic.metadata.CountSeries;
import com.spotify.heroic.metadata.DeleteSeries;
import com.spotify.heroic.metadata.FindKeys;
import com.spotify.heroic.metadata.FindSeries;
import com.spotify.heroic.metadata.FindTags;
import com.spotify.heroic.metric.MetricType;
import com.spotify.heroic.metric.ResultGroups;
import com.spotify.heroic.metric.WriteMetric;
import com.spotify.heroic.metric.WriteResult;
import com.spotify.heroic.suggest.KeySuggest;
import com.spotify.heroic.suggest.MatchOptions;
import com.spotify.heroic.suggest.TagKeyCount;
import com.spotify.heroic.suggest.TagSuggest;
import com.spotify.heroic.suggest.TagValueSuggest;
import com.spotify.heroic.suggest.TagValuesSuggest;

import eu.toolchain.async.AsyncFramework;
import eu.toolchain.async.AsyncFuture;

@RequiredArgsConstructor
public class CoreClusterNodeGroup implements ClusterNodeGroup {
    private final AsyncFramework async;
    private final Collection<ClusterNode.Group> entries;

    @Override
    public Iterator<Group> iterator() {
        return entries.iterator();
    }

    @Override
    public ClusterNode node() {
        throw new IllegalStateException("No node associated with ClusterNodeGroups");
    }

    @Override
    public AsyncFuture<ResultGroups> query(MetricType source, Filter filter, List<String> groupBy,
            DateRange range, Aggregation aggregation, boolean disableCache) {
        final List<AsyncFuture<ResultGroups>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.query(source, filter, groupBy, range, aggregation, disableCache).catchFailed(
                    ResultGroups.nodeError(g)));
        }

        return async.collect(futures, ResultGroups.merger());
    }

    @Override
    public AsyncFuture<FindTags> findTags(RangeFilter filter) {
        final List<AsyncFuture<FindTags>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.findTags(filter).catchFailed(FindTags.nodeError(g)));
        }

        return async.collect(futures, FindTags.reduce());
    }

    @Override
    public AsyncFuture<FindKeys> findKeys(RangeFilter filter) {
        final List<AsyncFuture<FindKeys>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.findKeys(filter).catchFailed(FindKeys.nodeError(g)));
        }

        return async.collect(futures, FindKeys.reduce());
    }

    @Override
    public AsyncFuture<FindSeries> findSeries(RangeFilter filter) {
        final List<AsyncFuture<FindSeries>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.findSeries(filter).catchFailed(FindSeries.nodeError(g)));
        }

        return async.collect(futures, FindSeries.reduce(filter.getLimit()));
    }

    @Override
    public AsyncFuture<DeleteSeries> deleteSeries(RangeFilter filter) {
        final List<AsyncFuture<DeleteSeries>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.deleteSeries(filter).catchFailed(DeleteSeries.nodeError(g)));
        }

        return async.collect(futures, DeleteSeries.reduce());
    }

    @Override
    public AsyncFuture<CountSeries> countSeries(RangeFilter filter) {
        final List<AsyncFuture<CountSeries>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.countSeries(filter).catchFailed(CountSeries.nodeError(g)));
        }

        return async.collect(futures, CountSeries.reduce());
    }

    @Override
    public AsyncFuture<TagKeyCount> tagKeyCount(RangeFilter filter) {
        final List<AsyncFuture<TagKeyCount>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.tagKeyCount(filter).catchFailed(TagKeyCount.nodeError(g)));
        }

        return async.collect(futures, TagKeyCount.reduce(filter.getLimit()));
    }

    @Override
    public AsyncFuture<TagSuggest> tagSuggest(RangeFilter filter, MatchOptions options, String key, String value) {
        final List<AsyncFuture<TagSuggest>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.tagSuggest(filter, options, key, value).catchFailed(TagSuggest.nodeError(g)));
        }

        return async.collect(futures, TagSuggest.reduce(filter.getLimit()));
    }

    @Override
    public AsyncFuture<KeySuggest> keySuggest(RangeFilter filter, MatchOptions options, String key) {
        final List<AsyncFuture<KeySuggest>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.keySuggest(filter, options, key).catchFailed(KeySuggest.nodeError(g)));
        }

        return async.collect(futures, KeySuggest.reduce(filter.getLimit()));
    }

    @Override
    public AsyncFuture<TagValuesSuggest> tagValuesSuggest(RangeFilter filter, List<String> exclude, int groupLimit) {
        final List<AsyncFuture<TagValuesSuggest>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.tagValuesSuggest(filter, exclude, groupLimit).catchFailed(TagValuesSuggest.nodeError(g)));
        }

        return async.collect(futures, TagValuesSuggest.reduce(filter.getLimit(), groupLimit));
    }

    @Override
    public AsyncFuture<TagValueSuggest> tagValueSuggest(RangeFilter filter, String key) {
        final List<AsyncFuture<TagValueSuggest>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.tagValueSuggest(filter, key).catchFailed(TagValueSuggest.nodeError(g)));
        }

        return async.collect(futures, TagValueSuggest.reduce(filter.getLimit()));
    }

    @Override
    public AsyncFuture<WriteResult> writeSeries(DateRange range, Series series) {
        final List<AsyncFuture<WriteResult>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.writeSeries(range, series).catchFailed(WriteResult.nodeError(g)));
        }

        return async.collect(futures, WriteResult.merger());
    }

    @Override
    public AsyncFuture<WriteResult> writeMetric(WriteMetric write) {
        final List<AsyncFuture<WriteResult>> futures = new ArrayList<>(entries.size());

        for (final ClusterNode.Group g : entries) {
            futures.add(g.writeMetric(write).catchFailed(WriteResult.nodeError(g)));
        }

        return async.collect(futures, WriteResult.merger());
    }
}