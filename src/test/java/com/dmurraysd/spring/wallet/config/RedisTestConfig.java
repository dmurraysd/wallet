package com.dmurraysd.spring.wallet.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

import java.io.IOException;

@ActiveProfiles({"test", "locking-test"})
@TestConfiguration
public class RedisTestConfig {

    private final RedisServer redisServer;

    public RedisTestConfig(@Value("${spring.data.redis.port:6379}") int port) throws IOException {
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void startRedis() throws IOException {
        if (!redisServer.isActive()) {
            redisServer.start();
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        redisServer.stop();
    }
}
