package com.ssh.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuthResponse BDD Tests
 *
 * Purpose: Verify construction and behaviour for AuthResponse DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class AuthResponseTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set")
        void shouldSetFields_WhenConstructed() {
            // Given
            UserResponse user = new UserResponse(1L, "Thandi Nkosi", "thandi@ssh.co.za", "YOUTH", true);

            // When
            AuthResponse response = new AuthResponse("jwt.token.here", 86400000L, user);

            // Then
            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);
            assertThat(response.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("Given no explicit token type, When constructed, Then tokenType defaults to Bearer")
        void shouldDefaultToBearerTokenType() {
            // Given
            UserResponse user = new UserResponse(1L, "Thandi Nkosi", "thandi@ssh.co.za", "YOUTH", true);

            // When
            AuthResponse response = new AuthResponse("jwt.token.here", 86400000L, user);

            // Then
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("Given an empty response, When fields are set, Then they are stored")
        void shouldStoreFields_WhenSet() {
            // Given
            AuthResponse response = new AuthResponse();
            UserResponse user = new UserResponse(1L, "Thandi Nkosi", "thandi@ssh.co.za", "SME", true);

            // When
            response.setToken("new.token");
            response.setExpiresIn(3600000L);
            response.setUser(user);
            response.setTokenType("Custom");

            // Then
            assertThat(response.getToken()).isEqualTo("new.token");
            assertThat(response.getExpiresIn()).isEqualTo(3600000L);
            assertThat(response.getUser()).isEqualTo(user);
            assertThat(response.getTokenType()).isEqualTo("Custom");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given an auth response, When toString is called, Then token is not included")
        void shouldExcludeToken_WhenToStringCalled() {
            // Given
            UserResponse user = new UserResponse(1L, "Thandi Nkosi", "thandi@ssh.co.za", "YOUTH", true);
            AuthResponse response = new AuthResponse("jwt.token.here", 86400000L, user);

            // When
            String result = response.toString();

            // Then
            assertThat(result).doesNotContain("jwt.token.here");
            assertThat(result).contains("Bearer");
            assertThat(result).contains("86400000");
        }
    }
}
