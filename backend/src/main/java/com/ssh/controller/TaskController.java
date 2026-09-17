package com.ssh.controller;

import com.ssh.dto.request.TaskRequest;
import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.TaskResponse;
import com.ssh.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Task Controller
 *
 * Purpose: REST endpoints for creating, listing, and assigning tasks.
 *
 * WHY SSH needs this controller:
 *   - SMEs post tasks through POST /api/v1/tasks
 *   - Youth browse tasks through GET /api/v1/tasks
 *   - Assignment and completion happen through PATCH endpoints
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Create, list, and manage SME tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create a new task. Only SMEs can post.
     * The SME's identity is taken from the JWT (Authentication).
     */
    @PostMapping
    @Operation(summary = "Create a new task (SME only)")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
        @Valid @RequestBody TaskRequest request,
        Authentication authentication) {

        String smeEmail = authentication.getName();
        TaskResponse created = taskService.createTask(request, smeEmail);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Task created", created));
    }

    /**
     * List all open tasks. Any authenticated user can call this.
     */
    @GetMapping
    @Operation(summary = "List all open tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listOpenTasks() {
        List<TaskResponse> tasks = taskService.listOpenTasks();
        return ResponseEntity.ok(ApiResponse.success("Open tasks", tasks));
    }

    /**
     * Get a single task by its ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task found", task));
    }

    /**
     * Assign a task to a youth. Typically called by the SME who owns the task.
     */
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign a task to a youth")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
        @PathVariable Long id,
        @RequestParam Long youthId) {

        TaskResponse assigned = taskService.assignTask(id, youthId);
        return ResponseEntity.ok(ApiResponse.success("Task assigned", assigned));
    }

    /**
     * Mark a task as completed. Called by the assigned youth.
     */
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark a task as completed")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(@PathVariable Long id) {
        TaskResponse completed = taskService.completeTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task completed", completed));
    }
}
