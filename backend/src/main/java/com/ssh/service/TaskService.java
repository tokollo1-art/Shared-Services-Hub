package com.ssh.service;

import com.ssh.dto.request.TaskRequest;
import com.ssh.dto.response.TaskResponse;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.exception.ResourceNotFoundException;
import com.ssh.repository.SMERepository;
import com.ssh.repository.TaskRepository;
import com.ssh.repository.UserRepository;
import com.ssh.repository.YouthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Task Service
 *
 * Purpose: Business logic for creating, listing, and assigning tasks.
 *
 * WHY SSH needs this service:
 *   - SMEs create tasks through this service
 *   - Youth browse open tasks through this service
 *   - Assignment links a youth to a task
 *   - Every task creation is verified against the SME's existence and role
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SMERepository smeRepository;
    private final YouthRepository youthRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       SMERepository smeRepository,
                       YouthRepository youthRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.smeRepository = smeRepository;
        this.youthRepository = youthRepository;
    }

    /**
     * Create a new task. Only SMEs can post tasks.
     */
    @Transactional
    public TaskResponse createTask(TaskRequest request, String smeEmail) {
        // 1. Find the SME by the authenticated user's email
        User user = userRepository.findByEmail(smeEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", smeEmail));

        SME sme = smeRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException(
                "SME profile not found for user: " + smeEmail));

        // 2. Build the Task from the request
        Task task = new Task(
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            sme
        );
        task.setBudget(request.getBudget());
        task.setDurationDays(request.getDurationDays());
        task.setPaid(request.isPaid());

        // 3. Persist and return as DTO
        Task saved = taskRepository.save(task);
        return toTaskResponse(saved);
    }

    /**
     * List all open tasks. Any authenticated user can see this.
     */
    public List<TaskResponse> listOpenTasks() {
        return taskRepository.findByStatus("OPEN")
            .stream()
            .map(this::toTaskResponse)
            .collect(Collectors.toList());
    }

    /**
     * List all tasks posted by the authenticated SME.
     */
    public List<TaskResponse> listMyTasks(String smeEmail) {
        User user = userRepository.findByEmail(smeEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", smeEmail));

        SME sme = smeRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException(
                "SME profile not found for user: " + smeEmail));

        return taskRepository.findBySme(sme)
            .stream()
            .map(this::toTaskResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a single task by its ID.
     */
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return toTaskResponse(task);
    }

    /**
     * Assign a task to a youth. Called by an SME from the dashboard.
     */
    @Transactional
    public TaskResponse assignTask(Long taskId, Long youthId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        Youth youth = youthRepository.findById(youthId)
            .orElseThrow(() -> new ResourceNotFoundException("Youth", youthId));

        task.setAssignedTo(youth);
        task.setStatus("IN_PROGRESS");

        return toTaskResponse(taskRepository.save(task));
    }

    /**
     * Mark a task as completed by the assigned youth.
     */
    @Transactional
    public TaskResponse completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        task.setStatus("COMPLETED");
        return toTaskResponse(taskRepository.save(task));
    }

    /**
     * Convert a Task entity into a safe TaskResponse DTO.
     */
    private TaskResponse toTaskResponse(Task task) {
        TaskResponse response = new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getStatus()
        );

        response.setDescription(task.getDescription());
        response.setCategory(task.getCategory());
        response.setBudget(task.getBudget());
        response.setDurationDays(task.getDurationDays());
        response.setPaid(task.isPaid());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        // SME summary
        if (task.getSme() != null) {
            response.setSmeId(task.getSme().getId());
            response.setSmeBusinessName(task.getSme().getBusinessName());
        }

        // Assigned youth summary
        if (task.getAssignedTo() != null) {
            response.setAssignedToId(task.getAssignedTo().getId());
            response.setAssignedToName(task.getAssignedTo().getUser().getName());
        }

        return response;
    }
}
