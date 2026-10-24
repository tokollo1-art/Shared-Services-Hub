package com.ssh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS Configuration
 *
 * Purpose: Allow the SvelteKit frontend to call the SSH backend.
 *
 * WHY SSH needs this:
 *   - The frontend runs on http://localhost:5173 (dev)
 *   - Without CORS, browsers block cross-origin API requests
 *   - Production will use the real frontend URL
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Configuration
public class CorsConfig {

    /**
     * CorsConfigurationSource bean — provides CORS rules to Spring Security and MVC.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new AdvertisedHeadersCorsConfiguration();

        // Allow frontend dev origin(s). Add production URLs later.
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",   // SvelteKit dev
            "http://localhost:4173"    // SvelteKit preview
        ));

        // Allow all common HTTP methods
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allow all headers (Authorization, Content-Type, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Allow the browser to send credentials if needed (cookies, etc.)
        // Set to true only if we later add cookie-based auth.
        config.setAllowCredentials(true);

        // Cache preflight responses for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /**
     * CorsFilter bean — registered in the servlet filter chain.
     */
    @Bean
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }

    /**
     * CORS configuration that always advertises the allowed headers.
     *
     * WHY: Spring only echoes back the headers the browser asked for
     *      ("Access-Control-Request-Headers"). When a preflight request does
     *      not send that header, no "Access-Control-Allow-Headers" is written
     *      at all, so the allowed header set stays implicit. This variant
     *      falls back to the configured allowed headers instead.
     */
    private static class AdvertisedHeadersCorsConfiguration extends CorsConfiguration {

        @Override
        public List<String> checkHeaders(List<String> requestHeaders) {
            List<String> checked = super.checkHeaders(requestHeaders);
            if (checked == null || checked.isEmpty()) {
                return getAllowedHeaders();
            }
            return checked;
        }
    }
}
