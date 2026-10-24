package com.ssh.repository;

import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Task Repository
 *
 * Purpose: Database access for Task entities.
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findBySme(SME sme);

    List<Task> findByStatus(String status);

    List<Task> findByAssignedTo(Youth youth);

    /**
     * Count tasks in a given status assigned to a youth.
     * Used to count active reservations (IN_PROGRESS work).
     */
    long countByAssignedToAndStatus(Youth youth, String status);

    /**
     * Find all tasks in a specific category.
     * Categories: ADMIN, TECH, DESIGN, MARKETING, FINANCE, OPERATIONS.
     * Used for filtering and reporting.
     */
    List<Task> findByCategory(String category);

    /**
     * Find the first task assigned to a youth with one of the given statuses.
     * Used by AgentService to compute the youth's availability state.
     */
    Optional<Task> findFirstByAssignedToIdAndStatusIn(Long youthId, List<String> statuses);

    /**
     * Find all tasks assigned to a youth with one of the given statuses.
     * Used by AgentService to find awaiting-verification task IDs.
     */
    List<Task> findByAssignedToIdAndStatusIn(Long youthId, List<String> statuses);
}
