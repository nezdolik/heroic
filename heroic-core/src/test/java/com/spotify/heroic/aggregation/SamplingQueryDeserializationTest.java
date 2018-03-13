package com.spotify.heroic.aggregation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.heroic.HeroicMappers;
import com.spotify.heroic.common.Duration;
import com.spotify.heroic.grammar.QueryParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.spotify.heroic.test.Resources.openResource;
import static org.junit.Assert.assertEquals;

public class SamplingQueryDeserializationTest {
    private final ObjectMapper m = HeroicMappers.json(Mockito.mock(QueryParser.class));

    @Before
    public void setUp(){
        //m.configure(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES, true);
    }

    @Test
    public void deserializationTest() throws Exception {
        final Duration d = Duration.of(10, TimeUnit.MINUTES);

        try (final InputStream in = openResource(getClass(), "SamplingQuery.1.json")) {
            assertEquals(new SamplingQuery(Optional.of(d), Optional.empty()),
                m.readValue(in, SamplingQuery.class));
        }

        try (final InputStream in = openResource(getClass(), "SamplingQuery.2.json")) {
            assertEquals(new SamplingQuery(Optional.empty(), Optional.of(d)),
                m.readValue(in, SamplingQuery.class));
        }
    }
}
