package com.ticketing.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisCacheConfiguration {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(60)); // дефолт, если забудешь отдельный TTL

        RedisCacheConfiguration eventsCfg = defaults.entryTtl(Duration.ofSeconds(60));
        RedisCacheConfiguration seatsCfg  = defaults.entryTtl(Duration.ofSeconds(120));
        RedisCacheConfiguration eventByIdCfg = defaults.entryTtl(Duration.ofSeconds(60));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(Map.of(
                        "events", eventsCfg,
                        "eventSeats", seatsCfg,
                        "eventById", eventByIdCfg
                ))
                .build();
    }
}