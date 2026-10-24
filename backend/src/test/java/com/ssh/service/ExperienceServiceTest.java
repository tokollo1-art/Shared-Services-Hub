package com.ssh.service;

import com.ssh.entity.Experience;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.exception.BusinessRuleException;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.YouthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock private ExperienceRepository experienceRepository;
    @Mock private YouthRepository youthRepository;

    private ExperienceService experienceService;
    private Task task;
    private Youth youth;

    @BeforeEach
    void setUp() {
        experienceService = new ExperienceService(experienceRepository, youthRepository);

        User youthUser = new User("Sipho", "sipho@ssh.co.za", "hash", "YOUTH");
        youthUser.setId(3L);
        youth = new Youth(youthUser);
        youth.setId(30L);

        User smeUser = new User("Thandi", "thandi@ssh.co.za", "hash", "SME");
        SME sme = new SME(smeUser, "Thandi's Accounting");
        sme.setId(10L);
        task = new Task("Design a logo", "Brand identity", "DESIGN", sme);
        task.setId(100L);
    }

    @Test
    @DisplayName("First completion creates exactly one PENDING experience")
    void shouldCreateExperience_OnFirstCompletion() {
        when(experienceRepository.findFirstByTask(task)).thenReturn(Optional.empty());
        when(experienceRepository.save(any(Experience.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Experience experience = experienceService.recordCompletion(task, youth);

        assertThat(experience.getStatus()).isEqualTo("PENDING");
        assertThat(experience.isVerified()).isFalse();
        assertThat(experience.getTask()).isEqualTo(task);
        assertThat(experience.getYouth()).isEqualTo(youth);
        assertThat(experience.getStartDate()).isNotNull();
        verify(experienceRepository).save(any(Experience.class));
    }

    @Test
    @DisplayName("Resubmission reuses the existing experience instead of creating a duplicate")
    void shouldReuseExperience_OnResubmission() {
        Experience existing = new Experience(youth, task);
        existing.setId(500L);
        existing.reject("Fix the SKU column");
        when(experienceRepository.findFirstByTask(task)).thenReturn(Optional.of(existing));
        when(experienceRepository.save(existing)).thenReturn(existing);

        Experience result = experienceService.recordCompletion(task, youth);

        assertThat(result).isSameAs(existing);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getRejectionReason()).isEqualTo("Fix the SKU column");
        verify(experienceRepository).save(existing);
    }

    @Test
    @DisplayName("A verified experience cannot be submitted again")
    void shouldRejectCompletion_WhenExperienceIsVerified() {
        Experience verified = new Experience(youth, task);
        verified.verify("Great", 5.0, "ref");
        when(experienceRepository.findFirstByTask(task)).thenReturn(Optional.of(verified));

        assertThatThrownBy(() -> experienceService.recordCompletion(task, youth))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("verified");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejecting stores the revision reason on the experience")
    void shouldMarkExperienceRejected_WithReason() {
        Experience existing = new Experience(youth, task);
        existing.setId(500L);
        when(experienceRepository.findFirstByTask(task)).thenReturn(Optional.of(existing));
        when(experienceRepository.save(existing)).thenReturn(existing);

        experienceService.rejectForRevision(task, "Redo the SKU column");

        assertThat(existing.getStatus()).isEqualTo("REJECTED");
        assertThat(existing.getRejectionReason()).isEqualTo("Redo the SKU column");
        assertThat(existing.isVerified()).isFalse();
    }

    @Test
    @DisplayName("A missing completion record cannot be verified")
    void shouldRejectVerification_WhenNoExperienceExists() {
        when(experienceRepository.findFirstByTask(task)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> experienceService.requireForTask(task))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("must complete it first");
    }

    @Test
    @DisplayName("Resubmission clears the previous verification fields")
    void shouldClearVerificationFields_OnResubmission() {
        Experience existing = new Experience(youth, task);
        existing.setId(500L);
        existing.verify("Old feedback", 3.0, "old-ref");
        // A verified experience cannot be resubmitted, so test the entity
        // transition directly after a rejection.
        existing.reject("Needs work");

        existing.resubmit();

        assertThat(existing.getFeedback()).isNull();
        assertThat(existing.getRating()).isNull();
        assertThat(existing.getReference()).isNull();
        assertThat(existing.isVerified()).isFalse();
    }
}
