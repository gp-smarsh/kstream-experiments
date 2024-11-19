package com.kstreams.wordcount;

import com.kstreams.KafkaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = KafkaConfig.class)
public class WordCountApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordCountApplication.class, args);
    }

//    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
//    KafkaStreamsConfiguration kStreamsConfig(@Qualifier("wordCountKafkaStreamsConfig") KafkaStreamsConfiguration kStreamsConfig) {
//        return kStreamsConfig;
//    }
}
