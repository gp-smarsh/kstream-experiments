package com.kstreams.movieratings;

import com.kstreams.KafkaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = KafkaConfig.class)
public class MovieRatingsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieRatingsApplication.class, args);
    }
}
