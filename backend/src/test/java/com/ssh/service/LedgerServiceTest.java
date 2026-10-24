package com.ssh.service;

import com.ssh.entity.Experience;
import com.ssh.entity.LedgerEntry;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    private static final LocalDateTime VERIFIED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock private LedgerEntryRepository ledgerEntryRepository;

    private LedgerService ledgerService;
    private Experience firstExperience;
    private Experience secondExperience;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        ledgerService = new LedgerService(ledgerEntryRepository, clock);

        firstExperience = experience(500L, 100L, 30L);
        secondExperience = experience(501L, 101L, 31L);
    }

    @Test
    @DisplayName("The first entry chains from GENESIS and stores a SHA-256 hash")
    void shouldCreateGenesisEntry_WithHash() {
        when(ledgerEntryRepository.findByExperienceId(500L)).thenReturn(Optional.empty());
        when(ledgerEntryRepository.findTopByOrderBySequenceDesc()).thenReturn(Optional.empty());
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerEntry entry = ledgerService.append(firstExperience);

        assertThat(entry.getSequence()).isEqualTo(1L);
        assertThat(entry.getPreviousHash()).isEqualTo(LedgerService.GENESIS_HASH);
        assertThat(entry.getHash()).hasSize(64);
        assertThat(entry.getHash()).isEqualTo(
            LedgerService.sha256(LedgerService.canonical(entry)));
    }

    @Test
    @DisplayName("The second entry links to the first entry's hash")
    void shouldChainSecondEntry_ToFirst() {
        LedgerEntry first = entry(1L, firstExperience, LedgerService.GENESIS_HASH);
        first.setHash(LedgerService.sha256(LedgerService.canonical(first)));

        when(ledgerEntryRepository.findByExperienceId(501L)).thenReturn(Optional.empty());
        when(ledgerEntryRepository.findTopByOrderBySequenceDesc()).thenReturn(Optional.of(first));
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerEntry second = ledgerService.append(secondExperience);

        assertThat(second.getSequence()).isEqualTo(2L);
        assertThat(second.getPreviousHash()).isEqualTo(first.getHash());
        assertThat(second.getHash()).isNotEqualTo(first.getHash());
    }

    @Test
    @DisplayName("Appending is idempotent per experience")
    void shouldNotAppendTwice_ForTheSameExperience() {
        LedgerEntry existing = entry(1L, firstExperience, LedgerService.GENESIS_HASH);
        when(ledgerEntryRepository.findByExperienceId(500L)).thenReturn(Optional.of(existing));

        LedgerEntry result = ledgerService.append(firstExperience);

        assertThat(result).isSameAs(existing);
    }

    @Test
    @DisplayName("A valid chain passes verification")
    void shouldVerifyIntactChain() {
        LedgerEntry first = entry(1L, firstExperience, LedgerService.GENESIS_HASH);
        first.setHash(LedgerService.sha256(LedgerService.canonical(first)));
        LedgerEntry second = entry(2L, secondExperience, first.getHash());
        second.setHash(LedgerService.sha256(LedgerService.canonical(second)));

        when(ledgerEntryRepository.findAllByOrderBySequenceAsc())
            .thenReturn(List.of(first, second));

        LedgerService.ChainVerification result = ledgerService.verifyChain();

        assertThat(result.valid()).isTrue();
        assertThat(result.checkedEntries()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tampering with stored feedback invalidates the chain")
    void shouldDetectTamperedEntry() {
        LedgerEntry first = entry(1L, firstExperience, LedgerService.GENESIS_HASH);
        first.setHash(LedgerService.sha256(LedgerService.canonical(first)));
        LedgerEntry second = entry(2L, secondExperience, first.getHash());
        second.setHash(LedgerService.sha256(LedgerService.canonical(second)));

        second.setFeedback("Tampered after verification");

        when(ledgerEntryRepository.findAllByOrderBySequenceAsc())
            .thenReturn(List.of(first, second));

        LedgerService.ChainVerification result = ledgerService.verifyChain();

        assertThat(result.valid()).isFalse();
        assertThat(result.brokenAtSequence()).isEqualTo(2L);
        assertThat(result.message()).contains("modified");
    }

    @Test
    @DisplayName("One experience can be verified against the chain")
    void shouldVerifySingleExperience() {
        LedgerEntry first = entry(1L, firstExperience, LedgerService.GENESIS_HASH);
        first.setHash(LedgerService.sha256(LedgerService.canonical(first)));
        LedgerEntry second = entry(2L, secondExperience, first.getHash());
        second.setHash(LedgerService.sha256(LedgerService.canonical(second)));

        when(ledgerEntryRepository.findByExperienceId(501L)).thenReturn(Optional.of(second));
        when(ledgerEntryRepository.findAllByOrderBySequenceAsc())
            .thenReturn(List.of(first, second));

        LedgerService.ChainVerification result = ledgerService.verifyExperience(501L);

        assertThat(result.valid()).isTrue();
        assertThat(result.checkedEntries()).isEqualTo(2);
    }

    private Experience experience(Long experienceId, Long taskId, Long youthId) {
        User youthUser = new User("Youth " + youthId, "youth" + youthId + "@ssh.co.za", "hash", "YOUTH");
        youthUser.setId(youthId);
        Youth youth = new Youth(youthUser);
        youth.setId(youthId);

        User smeUser = new User("SME", "sme@ssh.co.za", "hash", "SME");
        SME sme = new SME(smeUser, "SME Co");
        Task task = new Task("Task " + taskId, "Description", "OPS", sme);
        task.setId(taskId);

        Experience experience = new Experience(youth, task);
        experience.setId(experienceId);
        experience.verify("Great work", 5.0, "https://ref.example.com/" + experienceId);
        experience.setVerifiedAt(VERIFIED_AT);
        return experience;
    }

    private LedgerEntry entry(Long sequence, Experience experience, String previousHash) {
        return new LedgerEntry(
            sequence,
            experience.getId(),
            experience.getTask().getId(),
            experience.getYouth().getId(),
            experience.getRating(),
            experience.getFeedback(),
            experience.getReference(),
            experience.getVerifiedAt(),
            previousHash
        );
    }
}
