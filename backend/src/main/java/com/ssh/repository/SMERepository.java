package com.ssh.repository;

import com.ssh.entity.SME;
import com.ssh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SME Repository
 *
 * Purpose: Database access for SME entities.
 *
 * WHY SSH needs this repository:
 *   - Look up SME profiles by their User account
 *   - Find verified SMEs (only these can post tasks)
 *   - Find SMEs by industry (for reporting)
 *   - Count active SMEs for dashboards
 *
 * TODO: Add method to find SMEs by B-BBEE level
 * TODO: Add method to search SMEs by business name
 *
 * TDD: Write SMERepositoryTest first
 *       - shouldFindSME_ByUser()
 *       - shouldReturnEmpty_WhenUserHasNoSME()
 *       - shouldFindVerifiedSMEs()
 *       - shouldFindSMEs_ByIndustry()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface SMERepository extends JpaRepository<SME, Long> {

    /**
     * Find an SME by their linked User account.
     * Used when the logged-in user accesses their SME profile.
     */
    Optional<SME> findByUser(User user);

    /**
     * Find an SME by their business name.
     * Used for admin lookup and reporting.
     */
    Optional<SME> findByBusinessName(String businessName);

    /**
     * Find all verified SMEs.
     * Only verified SMEs can post tasks.
     */
    List<SME> findByIsVerifiedTrue();

    /**
     * Find all SMEs in a specific industry.
     * Used for industry-based reporting.
     */
    List<SME> findByIndustry(String industry);

    // TODO: Add long countByIsVerifiedTrue();
    // TODO: Add List<SME> findByBbbeeLevel(String bbbeeLevel);
    // TODO: Add List<SME> findByBusinessNameContainingIgnoreCase(String name);
}
