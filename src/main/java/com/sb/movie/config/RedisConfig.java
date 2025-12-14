package com.sb.movie.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.cache.redis.time-to-live:3600000}") // 1 hour default
    private long defaultTTL;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        log.info("Configuring Redis connection to {}:{}", redisHost, redisPort);
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate configured successfully");
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis Cache Manager with default TTL: {} ms", defaultTTL);

        // Create ObjectMapper for serialization
        ObjectMapper mapper = objectMapper();
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(defaultTTL))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // Custom TTL for different caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Events cache - 2 hours (events don't change frequently)
        cacheConfigurations.put("events", defaultConfig.entryTtl(Duration.ofHours(2)));

        // Event by ID - 2 hours
        cacheConfigurations.put("eventById", defaultConfig.entryTtl(Duration.ofHours(2)));

        // Events by type - 1 hour
        cacheConfigurations.put("eventsByType", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Events by city - 1 hour
        cacheConfigurations.put("eventsByCity", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Events by genre - 1 hour
        cacheConfigurations.put("eventsByGenre", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Events by language - 1 hour
        cacheConfigurations.put("eventsByLanguage", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Events by date - 1 hour
        cacheConfigurations.put("eventsByDate", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Shows cache - 30 minutes (show availability changes frequently)
        cacheConfigurations.put("shows", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Show by ID - 15 minutes (to get near real-time seat availability)
        cacheConfigurations.put("showById", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Shows by event - 30 minutes
        cacheConfigurations.put("showsByEvent", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Shows by theater - 30 minutes
        cacheConfigurations.put("showsByTheater", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Shows by date - 30 minutes
        cacheConfigurations.put("showsByDate", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Shows grouped - 30 minutes
        cacheConfigurations.put("showsGrouped", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        log.info("Cache configurations created for: {}", cacheConfigurations.keySet());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * ObjectMapper configured for Redis serialization
     * Includes support for Java 8 time types and polymorphic types
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 time module for LocalDate, LocalDateTime, etc.
        mapper.registerModule(new JavaTimeModule());

        // Configure to handle missing properties (from @JsonBackReference)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);

        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable polymorphic type handling for proper deserialization
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return mapper;
    }
}
