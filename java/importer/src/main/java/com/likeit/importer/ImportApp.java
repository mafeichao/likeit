package com.likeit.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author mafeichao
 */
@Slf4j
@SpringBootApplication
public class ImportApp {
    public static void main(String[] args) {
        SpringApplication.run(ImportApp.class, args);
    }
}
