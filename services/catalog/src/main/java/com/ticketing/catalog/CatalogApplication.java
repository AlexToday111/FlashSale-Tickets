package com.ticketing.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CatalogApplication {
    public static void main(String[] args){
        SpringApplication.run(CatalogApplication.class, args);
    }
}
