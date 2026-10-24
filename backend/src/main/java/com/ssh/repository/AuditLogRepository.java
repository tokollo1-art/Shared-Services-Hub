package com.ssh.repository;

import com.ssh.entity.AuditLog;
import com.ssh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AuditLog Repository
 *
 * Purpose: Database access for AuditLog entities.
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUser(User user);

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByEntityId(Long entityId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find an audit log by its idempotency key.
     * Used by AgentService.saveAction() to make agent writes idempotent:
     * a repeat POST with the same key returns the original row, not a duplicate.
     */
    Optional<AuditLog> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find agent actions by actor type.
     * Used by the activity feed (actorType = "AGENT").
     */
    List<AuditLog> findByActorTypeOrderByCreatedAtDesc(String actorType);

    /**
     * Find the most recent agent actions.
     * Used by GET /api/v1/agent-activity?limit=N.
     */
    List<AuditLog> findTop50ByActorTypeOrderByCreatedAtDesc(String actorType);
}
