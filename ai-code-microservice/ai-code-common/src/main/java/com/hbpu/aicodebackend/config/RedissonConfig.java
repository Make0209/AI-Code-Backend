package com.hbpu.aicodebackend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson configuration based on Spring Boot Redis properties.
 */
@Configuration
@ConditionalOnClass(RedisProperties.class)
@ConditionalOnProperty(prefix = "spring.data.redis", name = "host")
public class RedissonConfig {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        String redisAddress = String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort());

        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(2)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(1)
                .setIdleConnectionTimeout(30000)
                .setConnectTimeout(5000)
                .setTimeout(3000)
                .setRetryAttempts(3);

        String password = redisProperties.getPassword();
        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
