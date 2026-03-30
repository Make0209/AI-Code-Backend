package com.hbpu.aicodebackend.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;
    private Integer port;
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        //设置redis的地址
        String redisAddress = String.format("redis://%s:%d", host, port);
        // 创建新的配置实例
        Config config = new Config();
        // 设置使用单服务并设置redis地址和数据库
        config.useSingleServer()
              .setAddress(redisAddress)
              .setDatabase(2)
              .setPassword(password)
              // 设置连接池大小，即最大连接数
              .setConnectionPoolSize(10)
              // 设置最小空闲连接数
              .setConnectionMinimumIdleSize(1)
              // 设置空闲连接的超时时间
              .setIdleConnectionTimeout(30000)
              // 设置连接超时时间
              .setConnectTimeout(5000)
              // 设置操作超时时间
              .setTimeout(3000)
              // 设置重试次数
              .setRetryAttempts(3);
        // 返回新的RedissonClient对象
        return Redisson.create(config);
    }
}