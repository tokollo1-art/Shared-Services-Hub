package com.ssh.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Configuration
 *
 * Purpose: Configure Redis for caching and session management.
 *
 * WHY SSH needs this:
 *   - Fast caching for task lists and matching results
 *   - JWT session storage (optional, currently stateless)
 *   - Rate-limiting counters in the future
 *
 * NOTE: Spring Data Redis 4.x deprecated GenericJackson2JsonRedisSerializer
 *       (the Jackson 2 variant) in favour of GenericJacksonJsonRedisSerializer
 *       (Jackson 3). The cache manager below keeps String serialization for
 *       values to avoid serializer issues. Values that must be cached as
 *       objects will be JSON-encoded by the caller.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * RedisTemplate for string-keyed operations.
     *
     * WHY the JSON serializer is used WITHOUT default typing:
     *   RedisSerializer.json() enables "unsafe default typing", which writes a
     *   "@class" type id for every value. That type id is only emitted for
     *   NON-FINAL runtime types, so an immutable map (Map.of(...) is a final
     *   java.util.ImmutableCollections$MapN) is written WITHOUT a type id while
     *   the reader still demands one -> "missing type id property '@class'".
     *   Serialising plain JSON avoids that mismatch entirely.
     *
     * CONSEQUENCE: values are read back as generic JSON structures
     *   (Map / List / primitives), not as the original POJO. Callers that need
     *   a typed value must convert it (e.g. ObjectMapper.convertValue).
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonValueSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonValueSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * JSON value serializer (Jackson 3) that keeps the stored payload readable.
     */
    private RedisSerializer<Object> jsonValueSerializer() {
        return GenericJacksonJsonRedisSerializer.builder().build();
    }

    /**
     * CacheManager with 1-hour default TTL.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
