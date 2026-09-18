package com.ssh.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserResponse BDD Tests
 *
 * Purpose: Verify construction and behaviour for UserResponse DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class UserResponseTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            UserResponse response = new UserResponse(
                1L,
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "YOUTH",
                true
            );

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Thandi Nkosi");
            assertThat(response.getEmail()).isEqualTo("thandi@ssh.co.za");
            assertThat(response.getRole()).isEqualTo("YOUTH");
            assertThat(response.isActive()).isTrue();
        }

        @Test
        @DisplayName("Given no arguments, When constructed, Then fields are null")
        void shouldHaveNullFields_WhenDefaultConstructed() {
            // Given / When
            UserResponse response = new UserResponse();

            // Then
            assertThat(response.getId()).isNull();
            assertThat(response.getName()).isNull();
            assertThat(response.getEmail()).isNull();
            assertThat(response.getRole()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("Given an empty response, When fields are set, Then they are stored")
        void shouldStoreFields_WhenSet() {
            // Given
            UserResponse response = new UserResponse();
            LocalDateTime now = LocalDateTime.now();

            // When
            response.setId(2L);
            response.setName("Sipho Dlamini");
            response.setEmail("sipho@ssh.co.za");
            response.setRole("SME");
            response.setActive(false);
            response.setCreatedAt(now);
            response.setUpdatedAt(now);

            // Then
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getName()).isEqualTo("Sipho Dlamini");
            assertThat(response.getEmail()).isEqualTo("sipho@ssh.co.za");
            assertThat(response.getRole()).isEqualTo("SME");
            assertThat(response.isActive()).isFalse();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Password Safety")
    class PasswordSafety {

        @Test
        @DisplayName("Given a UserResponse, When inspecting fields, Then there is no password field")
        void shouldNotHavePasswordField() {
            // Given / When
            UserResponse response = new UserResponse(
                1L,
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "YOUTH",
                true
            );

            // Then — verify the class has no password-related getter
            assertThat(UserResponse.class.getDeclaredMethods())
                .noneMatch(m -> m.getName().toLowerCase().contains("password"));
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a user response, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            UserResponse response = new UserResponse(
                1L,
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "YOUTH",
                true
            );

            // When
            String result = response.toString();

            // Then
            assertThat(result).contains("Thandi Nkosi");
            assertThat(result).contains("thandi@ssh.co.za");
            assertThat(result).contains("YOUTH");
        }
    }
}
