package com.ssh.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TaskStatus BDD Tests
 *
 * Purpose: Verify the task state machine allows exactly the agreed
 *          transitions and rejects everything else.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class TaskStatusTest {

    @Nested
    @DisplayName("Forward Flow")
    class ForwardFlow {

        @Test
        @DisplayName("Given OPEN, When moving to IN_PROGRESS, Then it is allowed")
        void shouldAllowOpenToInProgress() {
            assertThat(TaskStatus.OPEN.canTransitionTo(TaskStatus.IN_PROGRESS)).isTrue();
        }

        @Test
        @DisplayName("Given IN_PROGRESS, When moving to COMPLETED, Then it is allowed")
        void shouldAllowInProgressToCompleted() {
            assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("Given COMPLETED, When moving to VERIFIED, Then it is allowed")
        void shouldAllowCompletedToVerified() {
            assertThat(TaskStatus.COMPLETED.canTransitionTo(TaskStatus.VERIFIED)).isTrue();
        }
    }

    @Nested
    @DisplayName("Revision Path")
    class RevisionPath {

        @Test
        @DisplayName("Given COMPLETED, When sent back, Then it returns to IN_PROGRESS")
        void shouldAllowCompletedToInProgressForRevision() {
            assertThat(TaskStatus.COMPLETED.canTransitionTo(TaskStatus.IN_PROGRESS)).isTrue();
        }

        @Test
        @DisplayName("Given a revised IN_PROGRESS task, When resubmitted, Then it can be COMPLETED again")
        void shouldAllowResubmissionAfterRevision() {
            assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.COMPLETED)).isTrue();
            assertThat(TaskStatus.COMPLETED.canTransitionTo(TaskStatus.VERIFIED)).isTrue();
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class Cancellation {

        @Test
        @DisplayName("Given OPEN or IN_PROGRESS, When cancelled, Then it is allowed")
        void shouldAllowCancellationBeforeCompletion() {
            assertThat(TaskStatus.OPEN.canTransitionTo(TaskStatus.CANCELLED)).isTrue();
            assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.CANCELLED)).isTrue();
        }
    }

    @Nested
    @DisplayName("Rejected Transitions")
    class RejectedTransitions {

        @Test
        @DisplayName("Given OPEN, When completed or verified directly, Then it is rejected")
        void shouldRejectSkippingInProgress() {
            assertThat(TaskStatus.OPEN.canTransitionTo(TaskStatus.COMPLETED)).isFalse();
            assertThat(TaskStatus.OPEN.canTransitionTo(TaskStatus.VERIFIED)).isFalse();
        }

        @Test
        @DisplayName("Given IN_PROGRESS, When assigned again or verified, Then it is rejected")
        void shouldRejectRepeatAssignmentAndEarlyVerification() {
            assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.IN_PROGRESS)).isFalse();
            assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.VERIFIED)).isFalse();
        }

        @Test
        @DisplayName("Given COMPLETED, When completed again, Then it is rejected")
        void shouldRejectRepeatCompletion() {
            assertThat(TaskStatus.COMPLETED.canTransitionTo(TaskStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("Given COMPLETED, When cancelled, Then it is rejected")
        void shouldRejectCancellationAfterCompletion() {
            assertThat(TaskStatus.COMPLETED.canTransitionTo(TaskStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("Given VERIFIED, When moved anywhere, Then it is rejected")
        void shouldRejectAnyTransitionFromVerified() {
            for (TaskStatus next : TaskStatus.values()) {
                assertThat(TaskStatus.VERIFIED.canTransitionTo(next)).isFalse();
            }
        }

        @Test
        @DisplayName("Given CANCELLED, When moved anywhere, Then it is rejected")
        void shouldRejectAnyTransitionFromCancelled() {
            for (TaskStatus next : TaskStatus.values()) {
                assertThat(TaskStatus.CANCELLED.canTransitionTo(next)).isFalse();
            }
        }

        @Test
        @DisplayName("Given any status, When the target is null, Then it is rejected")
        void shouldRejectNullTarget() {
            for (TaskStatus current : TaskStatus.values()) {
                assertThat(current.canTransitionTo(null)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Parsing")
    class Parsing {

        @Test
        @DisplayName("Given a stored status, When parsed, Then the matching enum is returned")
        void shouldParseStoredStatus() {
            assertThat(TaskStatus.from("OPEN")).isEqualTo(TaskStatus.OPEN);
            assertThat(TaskStatus.from("in_progress")).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(TaskStatus.from(" Completed ")).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        @DisplayName("Given an unknown status, When parsed, Then an exception is thrown")
        void shouldRejectUnknownStatus() {
            assertThatThrownBy(() -> TaskStatus.from("PENDING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("Given a null status, When parsed, Then an exception is thrown")
        void shouldRejectNullStatus() {
            assertThatThrownBy(() -> TaskStatus.from(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
