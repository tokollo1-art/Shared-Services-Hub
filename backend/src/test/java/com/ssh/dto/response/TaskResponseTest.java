package com.ssh.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TaskResponse BDD Tests
 *
 * Purpose: Verify construction and behaviour for TaskResponse DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class TaskResponseTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            TaskResponse response = new TaskResponse(1L, "Design a logo", "OPEN");

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Design a logo");
            assertThat(response.getStatus()).isEqualTo("OPEN");
        }

        @Test
        @DisplayName("Given no arguments, When constructed, Then fields are null")
        void shouldHaveNullFields_WhenDefaultConstructed() {
            // Given / When
            TaskResponse response = new TaskResponse();

            // Then
            assertThat(response.getId()).isNull();
            assertThat(response.getTitle()).isNull();
            assertThat(response.getStatus()).isNull();
            assertThat(response.getSmeId()).isNull();
            assertThat(response.getAssignedToId()).isNull();
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("Given an empty response, When task fields are set, Then they are stored")
        void shouldStoreTaskFields_WhenSet() {
            // Given
            TaskResponse response = new TaskResponse();
            LocalDateTime now = LocalDateTime.now();

            // When
            response.setId(1L);
            response.setTitle("Design a logo");
            response.setDescription("Create a brand identity");
            response.setCategory("DESIGN");
            response.setStatus("IN_PROGRESS");
            response.setBudget(1000.0);
            response.setDurationDays(14);
            response.setPaid(true);
            response.setCreatedAt(now);
            response.setUpdatedAt(now);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Design a logo");
            assertThat(response.getDescription()).isEqualTo("Create a brand identity");
            assertThat(response.getCategory()).isEqualTo("DESIGN");
            assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
            assertThat(response.getBudget()).isEqualTo(1000.0);
            assertThat(response.getDurationDays()).isEqualTo(14);
            assertThat(response.isPaid()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Given an empty response, When SME summary is set, Then it is stored")
        void shouldStoreSmeSummary_WhenSet() {
            // Given
            TaskResponse response = new TaskResponse();

            // When
            response.setSmeId(10L);
            response.setSmeBusinessName("Thandi's Accounting");

            // Then
            assertThat(response.getSmeId()).isEqualTo(10L);
            assertThat(response.getSmeBusinessName()).isEqualTo("Thandi's Accounting");
        }

        @Test
        @DisplayName("Given an empty response, When assigned youth summary is set, Then it is stored")
        void shouldStoreAssignedYouthSummary_WhenSet() {
            // Given
            TaskResponse response = new TaskResponse();

            // When
            response.setAssignedToId(20L);
            response.setAssignedToName("Sipho Dlamini");

            // Then
            assertThat(response.getAssignedToId()).isEqualTo(20L);
            assertThat(response.getAssignedToName()).isEqualTo("Sipho Dlamini");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a task response, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            TaskResponse response = new TaskResponse(1L, "Design a logo", "OPEN");
            response.setCategory("DESIGN");
            response.setBudget(1000.0);

            // When
            String result = response.toString();

            // Then
            assertThat(result).contains("Design a logo");
            assertThat(result).contains("DESIGN");
            assertThat(result).contains("OPEN");
            assertThat(result).contains("1000.0");
        }
    }
}
