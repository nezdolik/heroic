package com.spotify.heroic.metrics.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import com.spotify.heroic.async.Callback;
import com.spotify.heroic.async.CancelReason;
import com.spotify.heroic.backend.QueryException;
import com.spotify.heroic.metrics.MetricBackend;
import com.spotify.heroic.metrics.model.FindRows;
import com.spotify.heroic.metrics.model.PreparedGroup;
import com.spotify.heroic.metrics.model.RowGroups;
import com.spotify.heroic.model.TimeSerie;

@RequiredArgsConstructor
public final class FindRowGroupsReducer implements
        Callback.Reducer<FindRows.Result, RowGroups> {
    @Override
    public RowGroups resolved(Collection<FindRows.Result> results,
            Collection<Exception> errors, Collection<CancelReason> cancelled)
            throws Exception {
        return new RowGroups(prepareGroups(results));
    }

    private final Map<TimeSerie, List<PreparedGroup>> prepareGroups(
            Collection<FindRows.Result> results) throws QueryException {

        final Map<TimeSerie, List<PreparedGroup>> queries = new HashMap<TimeSerie, List<PreparedGroup>>();

        for (final FindRows.Result result : results) {
            final MetricBackend backend = result.getBackend();

            for (final Map.Entry<TimeSerie, Set<TimeSerie>> entry : result.getRowGroups().entrySet()) {
                final TimeSerie slice = entry.getKey();

                List<PreparedGroup> groups = queries.get(slice);

                if (groups == null) {
                    groups = new ArrayList<PreparedGroup>();
                    queries.put(slice, groups);
                }

                for (final TimeSerie timeSerie : entry.getValue()) {
                    groups.add(new PreparedGroup(backend, timeSerie));
                }
            }
        }

        return queries;
    }
}