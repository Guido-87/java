package com.spring.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class ApplicationPropertiesTest {

    @Test
    void keepsLazyInitializationAndSkipsUnusedRedisRepositories() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            properties.load(input);
        }

        assertEquals("true", properties.getProperty("spring.main.lazy-initialization"));
        assertEquals("false", properties.getProperty("spring.data.redis.repositories.enabled"));
    }
}
