package org.vladimir.infotecs.keyvaluedb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.logging.Logger;

@SpringBootApplication
public class KeyValueDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyValueDbApplication.class, args);
    }
    @Bean
    public Logger logger() {
        return Logger.getLogger("logger");
    }

}
