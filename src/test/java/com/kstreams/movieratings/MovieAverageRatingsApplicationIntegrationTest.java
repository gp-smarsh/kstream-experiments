package com.kstreams.movieratings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kstreams.wordcount.WordCountApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest(classes = WordCountApplication.class)
class MovieAverageRatingsApplicationIntegrationTest {

    private final BlockingQueue<String> output = new LinkedBlockingQueue<>();

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    @TempDir
    private static File tempDir;

    private KafkaMessageListenerContainer<Integer, String> consumer;

    @Autowired
    private MovieRatingsKafkaProducer kafkaProducer;

    @BeforeEach
    public void setUp() {
        output.clear();
        createConsumer();
    }

    @Test
    void givenWhenMoviesAndRatingsArePosted_ThenAverageMovieRatingIsCalculated() throws Exception {
        startOutputTopicConsumer();

        postMovie(new Movie(1L, "Movie 1", 2021));

        postRatings(new Rating(1L, 1L, 5));
        postRatings(new Rating(2L, 1L, 4));
        postRatings(new Rating(3L, 1L, 3));

        // assert correct counts from output topic
        assertThat(output.poll(2, MINUTES)).isEqualTo("1:RatedMovie[id=1, title=Movie 1, releaseYear=2021, rating=4.0]");
    }

    private void postMovie(Movie movie) throws JsonProcessingException {
        kafkaProducer.sendMessage(movie, "movies", Movie.class);
    }

    private void postRatings(Rating rating) throws JsonProcessingException {
        kafkaProducer.sendMessage(rating, "ratings", Rating.class);
    }


    private void createConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-movie-ratings");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, RatedMovieDeserializer.class);

        // set up the consumer for the word count output
        DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(props);
        ContainerProperties containerProperties = new ContainerProperties("movie-ratings");
        consumer = new KafkaMessageListenerContainer<>(cf, containerProperties);
        consumer.setBeanName("templateTests");

        consumer.setupMessageListener((MessageListener<Long, RatedMovie>) record -> {
            log.info("Record received: {}", record);
            output.add(record.key() + ":" + record.value());
        });
    }

    private void startOutputTopicConsumer() {
        consumer.start();
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.streams.state.dir", tempDir::getAbsolutePath);
    }

}