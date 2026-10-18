package com.gavioesfc.sorteio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SorteioApplication {

    public static void main(String[] args) {
        SpringApplication.run(SorteioApplication.class, args);
    }
}
