package com.ssh.repository;

import com.ssh.entity.Experience;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Experience Repository
 * Purpose: Database access for Experience entities (the Experience Ledger).
 * WHY SSH needs this repository:
 *   - Find experiences by youth (the experience ledger)
 *   - Find experiences by task
 *   - Find pending (unverified) experiences for SME review
 *   - Find verified experiences for impact reporting
 * TODO: Add method to count verified experiences per youth
 * TODO: Add method to find experiences in a date range
 * TODO: Add custom query for average rating
 * TDD: Write ExperienceRepositoryTest first
 *       - shouldFindExperiences_ByYouth()
 *       - shouldFindExperiences_ByTask()
 *       - shouldFindVerifiedExperiences()
 *       - shouldFindPendingExperiences()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    /**
     * Find all experiences recorded for a specific youth.
     * This is the youth's Experience Ledger.
     */
    List<Experience> findByYouth(Youth youth);

    /**
     * Find all experiences linked to a specific task.
     * Used when an SME wants to check the experience tied to a task.
     */
    List<Experience> findByTask(Task task);

    /**
     * Find all verified experiences.
     * Used for impact reporting (verified work records only).
     */
    List<Experience> findByIsVerifiedTrue();

    /**
     * Find all unverified experiences.
     * Used for the SME dashboard to show tasks awaiting verification.
     */
    List<Experience> findByIsVerifiedFalse();

    /**
     * Find experiences by status.
     * Statuses: PENDING, IN_PROGRESS, COMPLETED, VERIFIED, REJECTED.
     */
    List<Experience> findByStatus(String status);

    // TODO: Add long countByYouthAndIsVerifiedTrue(Youth youth);
    // TODO: Add @Query("SELECT AVG(e.rating) FROM Experience e WHERE e.task.sme = :sme")
    //       Double findAverageRatingForSme(SME sme);
}
