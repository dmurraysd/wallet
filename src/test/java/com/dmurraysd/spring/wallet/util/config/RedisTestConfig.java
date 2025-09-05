package com.dmurraysd.spring.wallet.util.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;
import org.springframework.test.context.ActiveProfiles;

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
        if( !redisServer.isActive()) {
            redisServer.start();
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        redisServer.stop();
    }
}
