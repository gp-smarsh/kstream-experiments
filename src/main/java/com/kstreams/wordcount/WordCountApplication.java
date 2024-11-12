package com.kstreams.wordcount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kstreams")
public class WordCountApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordCountApplication.class, args);
    }

}
