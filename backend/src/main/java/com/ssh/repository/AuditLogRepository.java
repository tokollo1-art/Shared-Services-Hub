package com.ssh.repository;

import com.ssh.entity.AuditLog;
import com.ssh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLog Repository
 *
 * Purpose: Database access for AuditLog entities.
 *
 * WHY SSH needs this repository:
 *   - Find audit logs by user (who did what)
 *   - Find audit logs by entity (what happened to a specific record)
 *   - Find audit logs by action (CREATE, UPDATE, DELETE, etc.)
 *   - Find recent activity for compliance and debugging
 *
 * TODO: Add method to count actions by type
 * TODO: Add method to find logs after a specific date
 * TODO: Add custom query for entity-specific history
 *
 * TDD: Write AuditLogRepositoryTest first
 *       - shouldFindLogs_ByUser()
 *       - shouldFindLogs_ByEntityType()
 *       - shouldFindLogs_ByAction()
 *       - shouldFindRecentLogs()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user.
     * Used for "activity history" on a user's profile.
     */
    List<AuditLog> findByUser(User user);

    /**
     * Find all audit logs for a specific entity type.
     * Entity types: USER, SME, YOUTH, TASK, EXPERIENCE, PAYMENT.
     */
    List<AuditLog> findByEntityType(String entityType);

    /**
     * Find all audit logs for a specific entity ID.
     * Used to trace the full history of a single record.
     */
    List<AuditLog> findByEntityId(Long entityId);

    /**
     * Find all audit logs for a specific action.
     * Actions: CREATE, UPDATE, DELETE, VERIFY, PAY, LOGIN, LOGOUT.
     */
    List<AuditLog> findByAction(String action);

    /**
     * Find audit logs recorded after a specific date.
     * Used for recent activity and compliance reports.
     */
    List<AuditLog> findByCreatedAtAfter(LocalDateTime date);

    // TODO: Add long countByAction(String action);
    // TODO: Add List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    // TODO: Add List<AuditLog> findTop10ByOrderByCreatedAtDesc();
}
