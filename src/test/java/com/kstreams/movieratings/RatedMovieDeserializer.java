package com.kstreams.movieratings;

import org.apache.kafka.common.serialization.Deserializer;

public class RatedMovieDeserializer implements Deserializer<RatedMovie> {

    public RatedMovieDeserializer() {
    }

    @Override
    public RatedMovie deserialize(String topic, byte[] data) {
        try {
            return StreamsSerde.serdeFor(RatedMovie.class).deserialize(topic, data);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing Rating", e);
        }
    }
}
