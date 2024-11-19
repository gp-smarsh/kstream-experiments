package com.kstreams;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.streams.state.dir}")
    private String stateStoreLocation;

    @Primary
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "movie-rating-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        // configure the state location to allow tests to use clean state for every run
        props.put(STATE_DIR_CONFIG, stateStoreLocation);

        return new KafkaStreamsConfiguration(props);
    }

    @Bean(name = "wordCountKafkaStreamsConfig")
    KafkaStreamsConfiguration wordCountKafkaConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "word-count-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // configure the state location to allow tests to use clean state for every run
        props.put(STATE_DIR_CONFIG, stateStoreLocation);

        return new KafkaStreamsConfiguration(props);
    }

    @Bean(name = "wordCountStreamsBuilder")
    StreamsBuilderFactoryBean wordCountStreamsBuilder(@Qualifier("wordCountKafkaStreamsConfig") KafkaStreamsConfiguration kStreamsConfig) {
        return new StreamsBuilderFactoryBean(kStreamsConfig);
    }

    @Bean
    NewTopic inputTopic() {
        return TopicBuilder.name("input-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic moviesTopic() {
        return TopicBuilder.name("movies")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic ratingsTopic() {
        return TopicBuilder.name("ratings")
                .partitions(1)
                .replicas(1)
                .build();
    }

}