package com.ssh.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SMEResponse BDD Tests
 *
 * Purpose: Verify construction and behaviour for SMEResponse DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class SMEResponseTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            SMEResponse response = new SMEResponse(1L, "Thandi's Accounting", "Finance", true);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getBusinessName()).isEqualTo("Thandi's Accounting");
            assertThat(response.getIndustry()).isEqualTo("Finance");
            assertThat(response.isVerified()).isTrue();
        }

        @Test
        @DisplayName("Given no arguments, When constructed, Then fields are null")
        void shouldHaveNullFields_WhenDefaultConstructed() {
            // Given / When
            SMEResponse response = new SMEResponse();

            // Then
            assertThat(response.getId()).isNull();
            assertThat(response.getBusinessName()).isNull();
            assertThat(response.getIndustry()).isNull();
            assertThat(response.getUserId()).isNull();
            assertThat(response.getUserName()).isNull();
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("Given an empty response, When SME fields are set, Then they are stored")
        void shouldStoreSmeFields_WhenSet() {
            // Given
            SMEResponse response = new SMEResponse();
            LocalDateTime now = LocalDateTime.now();

            // When
            response.setId(1L);
            response.setBusinessName("Thandi's Accounting");
            response.setRegistrationNumber("2020/123456/07");
            response.setIndustry("Finance");
            response.setDescription("Small accounting firm");
            response.setEmployeeCount(12);
            response.setBbbeeLevel("Level 2");
            response.setVerified(true);
            response.setCreatedAt(now);
            response.setUpdatedAt(now);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getBusinessName()).isEqualTo("Thandi's Accounting");
            assertThat(response.getRegistrationNumber()).isEqualTo("2020/123456/07");
            assertThat(response.getIndustry()).isEqualTo("Finance");
            assertThat(response.getDescription()).isEqualTo("Small accounting firm");
            assertThat(response.getEmployeeCount()).isEqualTo(12);
            assertThat(response.getBbbeeLevel()).isEqualTo("Level 2");
            assertThat(response.isVerified()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Given an empty response, When user summary is set, Then it is stored")
        void shouldStoreUserSummary_WhenSet() {
            // Given
            SMEResponse response = new SMEResponse();

            // When
            response.setUserId(10L);
            response.setUserName("Thandi Nkosi");

            // Then
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getUserName()).isEqualTo("Thandi Nkosi");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given an SME response, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            SMEResponse response = new SMEResponse(1L, "Thandi's Accounting", "Finance", true);

            // When
            String result = response.toString();

            // Then
            assertThat(result).contains("Thandi's Accounting");
            assertThat(result).contains("Finance");
            assertThat(result).contains("isVerified=true");
        }
    }
}
