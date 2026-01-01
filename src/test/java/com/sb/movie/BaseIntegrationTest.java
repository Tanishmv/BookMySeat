package com.sb.movie;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> TestContainersConfig.getPostgres().getJdbcUrl());
        registry.add("spring.datasource.username", () -> TestContainersConfig.getPostgres().getUsername());
        registry.add("spring.datasource.password", () -> TestContainersConfig.getPostgres().getPassword());
        registry.add("spring.data.redis.host", () -> TestContainersConfig.getRedis().getHost());
        registry.add("spring.data.redis.port", () -> TestContainersConfig.getRedis().getMappedPort(6379));
    }
}
