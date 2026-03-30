package com.hbpu.aicodebackend.ai.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.StoreType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedisChatMemoryStore configuration.
 */
@Configuration
public class RedisChatMemoryStoreConfig {

    /**
     * Create RedisChatMemoryStore using the standard Spring Redis configuration.
     */
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore(RedisProperties redisProperties,
                                                     @Value("${langchain4j.community.redis.ttl:0}") long ttl) {
        return RedisChatMemoryStore.builder()
                .host(redisProperties.getHost())
                .port(redisProperties.getPort())
                .user("default")
                .storeType(StoreType.STRING)
                .password(redisProperties.getPassword())
                .ttl(ttl)
                .build();
    }
}
