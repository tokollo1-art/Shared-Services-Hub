package com.ssh.repository;

import com.ssh.entity.User;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Youth Repository
 *
 * Purpose: Database access for Youth entities.
 *
 * WHY SSH needs this repository:
 *   - Look up youth profiles by their User account
 *   - Find youth by location (for matching with nearby SMEs)
 *   - Find active (non-alumni) youth currently in the hub
 *   - Find youth alumni (exited to permanent employment)
 *
 * TODO: Add method to count active youth
 * TODO: Add method to find youth by availability
 * TODO: Add custom JPQL query to search youth by skills
 *
 * TDD: Write YouthRepositoryTest first
 *       - shouldFindYouth_ByUser()
 *       - shouldReturnEmpty_WhenUserHasNoYouth()
 *       - shouldFindActiveYouth()
 *       - shouldFindAlumniYouth()
 *       - shouldFindYouth_ByLocation()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface YouthRepository extends JpaRepository<Youth, Long> {

    /**
     * Find a youth profile by their linked User account.
     * Used when the logged-in user accesses their youth profile.
     */
    Optional<Youth> findByUser(User user);

    /**
     * Find all youth who are still active in the hub.
     * "Active" means they have not yet become alumni.
     */
    List<Youth> findByIsAlumniFalse();

    /**
     * Find all youth alumni.
     * Used for impact reporting and tracking outcomes.
     */
    List<Youth> findByIsAlumniTrue();

    /**
     * Find youth by location.
     * Used for geographic matching with nearby SMEs.
     */
    List<Youth> findByLocation(String location);

    // TODO: Add long countByIsAlumniFalse();
    // TODO: Add List<Youth> findByAvailability(String availability);
    // TODO: Add @Query("SELECT y FROM Youth y WHERE y.skills LIKE %:skill%")
    //       List<Youth> findBySkillContaining(String skill);
}
