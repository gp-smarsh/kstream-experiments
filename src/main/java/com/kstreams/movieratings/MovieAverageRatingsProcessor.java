package com.kstreams.movieratings;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieAverageRatingsProcessor {
    private final Serde<Movie> movieSerde = StreamsSerde.serdeFor(Movie.class);
    private final Serde<Rating> ratingSerde = StreamsSerde.serdeFor(Rating.class);
    private final Serde<RatedMovie> ratedMovieSerde = StreamsSerde.serdeFor(RatedMovie.class);

    @Autowired
    void buildPipeline(@Qualifier("defaultKafkaStreamsBuilder") StreamsBuilder builder) {
        KTable<Long, Movie> movieTable = builder.stream("movies",
                        Consumed.with(Serdes.Long(), movieSerde))
                .map((key, movie) -> new KeyValue<>(movie.id(), movie))
                .peek((key, value) -> log.info("Incoming movies key[{}] value[{}]", key, value))
                .toTable(Materialized.with(Serdes.Long(), movieSerde));

        KTable<Long, Rating> ratingsTable = builder.stream("ratings", Consumed.with(Serdes.Long(), ratingSerde))
                .map((key, rating) -> new KeyValue<>(rating.id(), rating))
                .peek((key, value) -> log.info("Incoming movie rating key[{}] value[{}]", key, value))
                .toTable(Materialized.with(Serdes.Long(), ratingSerde));

        KTable<Long, RatingAggregator> averageRatings = ratingsTable
                .groupBy((key, rating) -> KeyValue.pair(rating.movieId(), rating.rating()))
                .aggregate(
                        () -> new RatingAggregator(0, 0),
                        (Long key, Integer newValue, RatingAggregator aggregate) -> {
                            log.info("Aggregating key[{}] newValue[{}] aggregate[{}]", key, newValue, aggregate);
                            return new RatingAggregator(aggregate.count() + 1, aggregate.sum() + newValue);
                        },
                        (Long key, Integer oldValue, RatingAggregator aggregate) -> new RatingAggregator(aggregate.count() - 1, aggregate.sum() - oldValue),
                        Materialized.with(Serdes.Long(), StreamsSerde.serdeFor(RatingAggregator.class))
                );

        averageRatings.toStream()
                .join(movieTable, (rating, movie) -> new RatedMovie(movie.id(), movie.title(), movie.releaseYear(), rating.average()))
                .peek((key, value) -> log.info("Movie rating key[{}] value[{}]", key, value))
                .to("movie-ratings", Produced.with(Serdes.Long(), ratedMovieSerde));
    }
}
