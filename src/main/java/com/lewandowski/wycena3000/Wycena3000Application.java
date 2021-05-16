package com.lewandowski.wycena3000;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class Wycena3000Application {

    public static void main(String[] args) {
        SpringApplication.run(Wycena3000Application.class, args);
    }

}
