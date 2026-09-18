package com.ssh.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SwaggerConfig BDD Tests
 * Purpose: Verify OpenAPI documentation loads and contains expected API info.
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class SwaggerConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Swagger UI")
    class SwaggerUi {

        @Test
        @DisplayName("Given the application is running, When I visit Swagger UI, Then it loads")
        void shouldLoadSwaggerUi_WhenApplicationIsRunning() throws Exception {
            // Given / When / Then
            mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
            // TODO: Verify Swagger UI is accessible (may require permitAll in SecurityConfig)
        }
    }

    @Nested
    @DisplayName("OpenAPI Docs")
    class OpenApiDocs {

        @Test
        @DisplayName("Given the application is running, When I request the API docs, Then JSON is returned")
        void shouldReturnJson_WhenApiDocsRequested() throws Exception {
            // Given / When / Then
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
            // TODO: Verify content type is application/json
        }

        @Test
        @DisplayName("Given the API docs, When inspected, Then the title is 'SSH API'")
        void shouldContainTitle_WhenApiDocsInspected() throws Exception {
            // Given / When / Then
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("SSH API"));
        }

        @Test
        @DisplayName("Given the API docs, When inspected, Then the version is '1.0'")
        void shouldContainVersion_WhenApiDocsInspected() throws Exception {
            // Given / When / Then
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("1.0"));
        }

        @Test
        @DisplayName("Given the API docs, When inspected, Then the bearerAuth security scheme exists")
        void shouldContainBearerAuth_WhenApiDocsInspected() throws Exception {
            // Given / When / Then
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
        }
    }
}
