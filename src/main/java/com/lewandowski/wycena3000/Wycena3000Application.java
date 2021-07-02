package com.lewandowski.wycena3000;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class Wycena3000Application {

    public static void main(String[] args) {
        SpringApplication.run(Wycena3000Application.class, args);
    }

}
