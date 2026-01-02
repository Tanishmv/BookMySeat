package com.sb.movie;

import com.sb.movie.services.BookingEventProducer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @MockBean
    protected BookingEventProducer bookingEventProducer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> TestContainersConfig.getPostgres().getJdbcUrl());
        registry.add("spring.datasource.username", () -> TestContainersConfig.getPostgres().getUsername());
        registry.add("spring.datasource.password", () -> TestContainersConfig.getPostgres().getPassword());
        registry.add("spring.data.redis.host", () -> TestContainersConfig.getRedis().getHost());
        registry.add("spring.data.redis.port", () -> TestContainersConfig.getRedis().getMappedPort(6379));
    }
}
