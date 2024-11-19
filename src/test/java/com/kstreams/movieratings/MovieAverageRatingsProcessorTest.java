package com.kstreams.movieratings;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

class MovieAverageRatingsProcessorTest {
    private MovieAverageRatingsProcessor movieAverageRatingsProcessor = new MovieAverageRatingsProcessor();

    @Test
    void givenInputMessages_whenProcessed_thenMovieAverageRatingsIsProduced() {
        // Given
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        movieAverageRatingsProcessor.buildPipeline(streamsBuilder);
        Topology topology = streamsBuilder.build();
        Properties props = new Properties();
        props.put(APPLICATION_ID_CONFIG, "test-movies");
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, props)) {

            TestInputTopic<Long, Movie> movieInputTopic = topologyTestDriver
                    .createInputTopic("movies", Serdes.Long().serializer(), StreamsSerde.serdeFor(Movie.class).serializer());

            TestInputTopic<Long, Rating> ratingInputTopic = topologyTestDriver
                    .createInputTopic("ratings", Serdes.Long().serializer(), StreamsSerde.serdeFor(Rating.class).serializer());

            TestOutputTopic<Long, RatedMovie> outputTopic = topologyTestDriver
                    .createOutputTopic("movie-ratings", Serdes.Long().deserializer(), StreamsSerde.serdeFor(RatedMovie.class).deserializer());

            // When
            movieInputTopic.pipeInput(1L, new Movie(1L, "Movie 1", 2021));

            ratingInputTopic.pipeInput(1L, new Rating(1L, 1L, 5));
            ratingInputTopic.pipeInput(2L, new Rating(2L, 1L, 4));
            ratingInputTopic.pipeInput(3L, new Rating(3L, 1L, 3));

            // Then
            assertThat(outputTopic.readKeyValuesToList())
                    .containsExactly(
                            KeyValue.pair(1L, new RatedMovie(1L, "Movie 1", 2021, 5.0)),
                            KeyValue.pair(1L, new RatedMovie(1L, "Movie 1", 2021, 4.5)),
                            KeyValue.pair(1L, new RatedMovie(1L, "Movie 1", 2021, 4.0))
                    );
        }
    }
}