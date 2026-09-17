package com.ssh.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfig BDD Tests
 * Purpose: Verify security behaviour at multiple levels.
 * Structure: BDD Given/When/Then, nested by scenario.
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("Password Encoding")
    class PasswordEncoding {

        @Test
        @DisplayName("Given a password, When encoded, Then it should not match plain text and should be verified")
        void shouldEncodePassword_WhenGivenPlainText() {
            // Given
            String rawPassword = "SecurePass123";

            // When
            String encoded = passwordEncoder.encode(rawPassword);

            // Then
            assertThat(encoded).isNotEqualTo(rawPassword);
            assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("Given unauthenticated user, When calling login, Then access is allowed")
        void shouldAllowLogin_WhenUnauthenticated() throws Exception {
            // Given / When / Then
            mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isUnauthorized());
            // TODO: change to isOk() after auth is implemented
        }

        @Test
        @DisplayName("Given unauthenticated user, When calling Swagger UI, Then access is allowed")
        void shouldAllowSwagger_WhenUnauthenticated() throws Exception {
            // TODO: Implement after SwaggerConfig is complete
        }
    }

    @Nested
    @DisplayName("Protected Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("Given no token, When calling protected endpoint, Then 401 is returned")
        void shouldReturn401_WhenNoToken() throws Exception {
            // Given / When / Then
            mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Given ADMIN role, When calling admin endpoint, Then access is allowed")
        void shouldAllowAdmin_WhenAdminRole() throws Exception {
            // TODO: Implement after AdminController exists
        }

        @Test
        @WithMockUser(roles = "YOUTH")
        @DisplayName("Given YOUTH role, When calling admin endpoint, Then 403 is returned")
        void shouldReturn403_WhenWrongRole() throws Exception {
            // TODO: Implement after AdminController exists
        }
    }
}
