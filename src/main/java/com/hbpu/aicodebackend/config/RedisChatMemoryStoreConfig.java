package com.hbpu.aicodebackend.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.StoreType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedisChatMemoryStore配置类，LangChain4j官方的链接Redis的配置类
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.redis")
@Data
public class RedisChatMemoryStoreConfig {

    /**
     * Redis的主机地址
     */
    private String host;
    /**
     * Redis的端口号
     */
    private int port;
    /**
     * Redis的密码
     */
    private String password;
    /**
     * Redis的过期时间
     */
    private long ttl;

    /**
     * 创建RedisChatMemoryStore
     * @return RedisChatMemoryStore
     */
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .user("default")
                .storeType(StoreType.STRING)
                .password(password)
                .ttl(ttl)
                .build();
    }
}
