package com.ssh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration
 *
 * Purpose: Configure web-related settings for SSH.
 *
 * WHY SSH needs this:
 *   - Provides a secondary CORS mapping for Spring MVC
 *   - Placeholder for future interceptors (request logging, rate limiting)
 *
 * NOTE: The primary CORS handling lives in CorsConfig (servlet filter level).
 *       This class adds WebMvc-level CORS for @Controller endpoints.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:4173")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
