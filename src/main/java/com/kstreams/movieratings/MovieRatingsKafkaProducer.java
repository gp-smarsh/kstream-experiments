package com.kstreams.movieratings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class MovieRatingsKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> void sendMessage(T message, String topic, Class<T> clazz) throws JsonProcessingException {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(message))
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent to topic: {}", message);
                    } else {
                        log.error("Failed to send message", ex);
                    }
                });
    }
}