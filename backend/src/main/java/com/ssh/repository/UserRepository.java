package com.ssh.repository;

import com.ssh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 *
 * Purpose: Database access for User entities.
 *
 * WHY SSH needs this repository:
 *   - Look up users by email (for authentication)
 *   - Find users by role (for admin dashboards)
 *   - Check if email already exists (for registration)
 *   - Count users by role (for reporting)
 *
 * TODO: Add method to find users created within a date range
 * TODO: Add method to search users by name
 *
 * TDD: Write UserRepositoryTest first
 *       - shouldFindUser_ByEmail()
 *       - shouldReturnEmpty_WhenEmailNotFound()
 *       - shouldReturnTrue_WhenEmailExists()
 *       - shouldReturnFalse_WhenEmailDoesNotExist()
 *       - shouldFindUsers_ByRole()
 *       - shouldFindActiveUsers()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email.
     * Used for authentication and registration validation.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     * Roles: YOUTH, SME, CORPORATE, ADMIN.
     * Used for admin dashboards to list user types.
     */
    List<User> findByRole(String role);

    /**
     * Find all active users.
     * Used for reports and dashboards to exclude soft-deleted users.
     */
    List<User> findByIsActiveTrue();

    // TODO: Add long countByRole(String role);
    // TODO: Add List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    // TODO: Add List<User> findByNameContainingIgnoreCase(String name);
}
