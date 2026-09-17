package com.ssh.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebConfig BDD Tests
 * Purpose: Verify WebConfig is loaded and implements WebMvcConfigurer.
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Nested
    @DisplayName("Bean Loading")
    class BeanLoading {

        @Test
        @DisplayName("Given the application context, When loaded, Then WebConfig bean is present")
        void shouldLoadWebConfigBean_WhenApplicationStarts() {
            // Given / When / Then
            assertThat(webConfig).isNotNull();
        }

        @Test
        @DisplayName("Given WebConfig, When inspected, Then it implements WebMvcConfigurer")
        void shouldImplementWebMvcConfigurer_WhenInspected() {
            // Given / When / Then
            assertThat(webConfig).isInstanceOf(WebMvcConfigurer.class);
        }
    }
}
