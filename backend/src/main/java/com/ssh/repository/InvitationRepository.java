package com.ssh.repository;

import com.ssh.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * InvitationRepository
 *
 * Purpose: Database access for Invitation entities.
 *
 * The matchmaker writes here when it invites a youth. The invitation
 * agent writes here when it withdraws pending invitations after an
 * assignment. The youth feed reads here to show a youth their invitations.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    /**
     * Find a specific invitation by task and youth.
     * Used to check for an existing row before inserting, so the service
     * can return the existing row (200) instead of a duplicate (201).
     */
    Optional<Invitation> findByTaskIdAndYouthId(Long taskId, Long youthId);

    /**
     * Find all invitations for a task with a given status.
     * Used by the SME task page to show who has been invited.
     */
    List<Invitation> findByTaskIdAndStatus(Long taskId, String status);

    /**
     * Find all invitations for a youth with a given status.
     * Used by the youth feed to show pending invitations.
     */
    List<Invitation> findByYouthIdAndStatus(Long youthId, String status);

    /**
     * Find all pending invitations for a youth, excluding one task.
     * Used by the invitation agent when TASK_ASSIGNED arrives:
     * "withdraw this youth's other pending invitations, except the one
     * they were just assigned to."
     */
    List<Invitation> findByYouthIdAndStatusAndTaskIdNot(
        Long youthId,
        String status,
        Long exceptTaskId
    );

    /**
     * Bulk-withdraw: update all pending invitations for a youth except
     * the one for the given task. One UPDATE instead of N per-row saves.
     * Returns the number of rows updated.
     */
    @Modifying
    @Query("""
        UPDATE Invitation i
        SET i.status = 'WITHDRAWN', i.updatedAt = CURRENT_TIMESTAMP
        WHERE i.youth.id = :youthId
          AND i.status = 'PENDING'
          AND i.task.id <> :exceptTaskId
        """)
    int withdrawPendingExcept(
        @Param("youthId") Long youthId,
        @Param("exceptTaskId") Long exceptTaskId
    );

    /**
     * Check whether a youth has any pending invitation for a task.
     * Used by the controller's idempotency check.
     */
    boolean existsByTaskIdAndYouthIdAndStatus(Long taskId, Long youthId, String status);
}
