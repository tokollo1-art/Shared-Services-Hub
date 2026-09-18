package com.ssh.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * YouthResponse BDD Tests
 *
 * Purpose: Verify construction and behaviour for YouthResponse DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class YouthResponseTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            YouthResponse response = new YouthResponse(1L, "Sipho Dlamini", "Johannesburg", false);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUserName()).isEqualTo("Sipho Dlamini");
            assertThat(response.getLocation()).isEqualTo("Johannesburg");
            assertThat(response.isAlumni()).isFalse();
        }

        @Test
        @DisplayName("Given no arguments, When constructed, Then fields are null")
        void shouldHaveNullFields_WhenDefaultConstructed() {
            // Given / When
            YouthResponse response = new YouthResponse();

            // Then
            assertThat(response.getId()).isNull();
            assertThat(response.getUserName()).isNull();
            assertThat(response.getLocation()).isNull();
            assertThat(response.getUserId()).isNull();
            assertThat(response.getAlumniDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("Given an empty response, When youth fields are set, Then they are stored")
        void shouldStoreYouthFields_WhenSet() {
            // Given
            YouthResponse response = new YouthResponse();
            LocalDateTime now = LocalDateTime.now();

            // When
            response.setId(1L);
            response.setEducation("BSc Computer Science");
            response.setSkills("Java, Spring Boot, PostgreSQL");
            response.setPortfolio("https://github.com/sipho");
            response.setBio("Recent graduate looking for opportunities");
            response.setLocation("Johannesburg");
            response.setAvailability("Immediate");
            response.setCreatedAt(now);
            response.setUpdatedAt(now);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEducation()).isEqualTo("BSc Computer Science");
            assertThat(response.getSkills()).isEqualTo("Java, Spring Boot, PostgreSQL");
            assertThat(response.getPortfolio()).isEqualTo("https://github.com/sipho");
            assertThat(response.getBio()).isEqualTo("Recent graduate looking for opportunities");
            assertThat(response.getLocation()).isEqualTo("Johannesburg");
            assertThat(response.getAvailability()).isEqualTo("Immediate");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Given an empty response, When user summary is set, Then it is stored")
        void shouldStoreUserSummary_WhenSet() {
            // Given
            YouthResponse response = new YouthResponse();

            // When
            response.setUserId(10L);
            response.setUserName("Sipho Dlamini");

            // Then
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getUserName()).isEqualTo("Sipho Dlamini");
        }

        @Test
        @DisplayName("Given an empty response, When alumni status is set, Then it is stored")
        void shouldStoreAlumniStatus_WhenSet() {
            // Given
            YouthResponse response = new YouthResponse();
            LocalDateTime alumniDate = LocalDateTime.now();

            // When
            response.setAlumni(true);
            response.setAlumniDate(alumniDate);

            // Then
            assertThat(response.isAlumni()).isTrue();
            assertThat(response.getAlumniDate()).isEqualTo(alumniDate);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a youth response, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            YouthResponse response = new YouthResponse(1L, "Sipho Dlamini", "Johannesburg", false);

            // When
            String result = response.toString();

            // Then
            assertThat(result).contains("Sipho Dlamini");
            assertThat(result).contains("Johannesburg");
            assertThat(result).contains("isAlumni=false");
        }
    }
}
