package com.ssh.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisConfig BDD Tests
 * Purpose: Verify Redis connection, serialization, and cache behaviour.
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Nested
    @DisplayName("Connection")
    class Connection {

        @Test
        @DisplayName("Given Redis is running, When I ping, Then PONG is returned")
        void shouldReturnPong_WhenPinged() {
            // Given / When / Then
            String response = stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
            assertThat(response).isEqualTo("PONG");
        }
    }

    @Nested
    @DisplayName("Store and Retrieve")
    class StoreAndRetrieve {

        @Test
        @DisplayName("Given a string value, When stored, Then it can be retrieved")
        void shouldStoreAndRetrieve_WhenStringValue() {
            // Given
            String key = "ssh:test:string";
            String value = "hello-ssh";

            // When
            stringRedisTemplate.opsForValue().set(key, value);
            String retrieved = stringRedisTemplate.opsForValue().get(key);

            // Then
            assertThat(retrieved).isEqualTo(value);

            // Cleanup
            stringRedisTemplate.delete(key);
        }

        @Test
        @DisplayName("Given a Java object, When stored, Then it can be retrieved with matching fields")
        void shouldStoreAndRetrieve_WhenJavaObject() {
            // Given
            String key = "ssh:test:object";
            Map<String, Object> payload = Map.of(
                "taskId", 42L,
                "title", "Design a logo",
                "budget", 1500.00
            );

            // When
            redisTemplate.opsForValue().set(key, payload);
            Object retrieved = redisTemplate.opsForValue().get(key);

            // Then
            assertThat(retrieved).isInstanceOf(Map.class);
            Map<?, ?> result = (Map<?, ?>) retrieved;
            assertThat(result.get("title")).isEqualTo("Design a logo");

            // Cleanup
            redisTemplate.delete(key);
        }
    }

    @Nested
    @DisplayName("TTL")
    class Ttl {

        @Test
        @DisplayName("Given a value with a short TTL, When TTL passes, Then the value is gone")
        void shouldExpireValue_AfterTtl() throws InterruptedException {
            // Given
            String key = "ssh:test:ttl";
            stringRedisTemplate.opsForValue().set(key, "expiring", Duration.ofSeconds(1));

            // When
            Thread.sleep(1500);

            // Then
            String retrieved = stringRedisTemplate.opsForValue().get(key);
            assertThat(retrieved).isNull();
        }
    }
}
