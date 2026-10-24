package com.ssh.repository;

import com.ssh.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    Optional<LedgerEntry> findTopByOrderBySequenceDesc();

    Optional<LedgerEntry> findByExperienceId(Long experienceId);

    List<LedgerEntry> findAllByOrderBySequenceAsc();
}
