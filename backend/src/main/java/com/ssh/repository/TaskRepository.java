package com.ssh.repository;

import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Task Repository
 *
 * Purpose: Database access for Task entities.
 *
 * WHY SSH needs this repository:
 *   - Find tasks posted by a specific SME
 *   - Find tasks with a specific status (OPEN, IN_PROGRESS, etc.)
 *   - Find tasks assigned to a specific youth
 *   - Find tasks by category (for reporting)
 *
 * TODO: Add method to count tasks by status
 * TODO: Add method to find tasks by category
 * TODO: Add paginated query for open tasks
 * TODO: Add custom JPQL to search tasks by title
 *
 * TDD: Write TaskRepositoryTest first
 *       - shouldFindTasks_BySme()
 *       - shouldFindTasks_ByStatus()
 *       - shouldFindTasks_ByAssignedYouth()
 *       - shouldFindTasks_ByCategory()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks posted by a specific SME.
     * Used for the SME dashboard.
     */
    List<Task> findBySme(SME sme);

    /**
     * Find all tasks with a specific status.
     * Statuses: OPEN, IN_PROGRESS, COMPLETED, VERIFIED, CANCELLED.
     * Used for the youth dashboard (find OPEN tasks) and admin reports.
     */
    List<Task> findByStatus(String status);

    /**
     * Find all tasks assigned to a specific youth.
     * Used for the youth dashboard to show their active tasks.
     */
    List<Task> findByAssignedTo(Youth youth);

    /**
     * Find all tasks in a specific category.
     * Categories: ADMIN, TECH, DESIGN, MARKETING, FINANCE, OPERATIONS.
     * Used for filtering and reporting.
     */
    List<Task> findByCategory(String category);

    // TODO: Add long countByStatus(String status);
    // TODO: Add Page<Task> findByStatus(String status, Pageable pageable);
    // TODO: Add List<Task> findByTitleContainingIgnoreCase(String title);
}
