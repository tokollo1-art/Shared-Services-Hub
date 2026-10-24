package com.ssh.repository;

import com.ssh.entity.Experience;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Experience Repository
 *
 * Purpose: Database access for Experience entities (the Experience Ledger).
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    /**
     * Find the single experience linked to a task.
     * The database enforces one experience per task.
     */
    java.util.Optional<Experience> findFirstByTask(Task task);

    /**
     * Whether a completion record already exists for a task.
     */
    boolean existsByTask(Task task);

    /**
     * Count only VERIFIED experiences for a youth. This is the sole input to
     * tier eligibility: pending, rejected or in-progress work never counts.
     */
    long countByYouthAndIsVerifiedTrue(Youth youth);

    /**
     * Find all experiences recorded for a specific youth.
     * This is the youth's Experience Ledger.
     */
    List<Experience> findByYouth(Youth youth);

    List<Experience> findByTask(Task task);

    List<Experience> findByIsVerifiedTrue();

    List<Experience> findByIsVerifiedFalse();

    List<Experience> findByStatus(String status);

    /**
     * Count a youth's verified experiences.
     * Used by AgentService to compute the youth's tier.
     */
    long countByYouthIdAndIsVerifiedTrue(Long youthId);

    /**
     * Count a youth's unverified experiences.
     * Used by AgentService to populate awaitingVerificationCount.
     */
    long countByYouthIdAndIsVerifiedFalse(Long youthId);
}
