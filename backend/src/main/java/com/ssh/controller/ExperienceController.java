package com.ssh.controller;

import com.ssh.dto.request.VerificationRequest;
import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.ExperienceResponse;
import com.ssh.dto.response.VerificationResponse;
import com.ssh.service.ExperienceService;
import com.ssh.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Experience Ledger read endpoints plus the experience-centric verification
 * alias. Verification itself is orchestrated by TaskService so the task,
 * experience, payment, ledger and event always change together.
 */
@RestController
@RequestMapping("/api/v1/experiences")
@Tag(name = "Experiences", description = "The Experience Ledger — verified work records")
public class ExperienceController {

    private final ExperienceService experienceService;
    private final TaskService taskService;

    public ExperienceController(ExperienceService experienceService,
                                TaskService taskService) {
        this.experienceService = experienceService;
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an experience by ID")
    public ResponseEntity<ApiResponse<ExperienceResponse>> getExperience(
        @PathVariable Long id) {

        ExperienceResponse experience = experienceService.getExperienceById(id);
        return ResponseEntity.ok(ApiResponse.success("Experience found", experience));
    }

    @GetMapping("/youth/{youthId}")
    @Operation(summary = "List all experiences for a youth")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> listForYouth(
        @PathVariable Long youthId) {

        List<ExperienceResponse> experiences =
            experienceService.listExperiencesForYouth(youthId);
        return ResponseEntity.ok(
            ApiResponse.success("Experience ledger", experiences));
    }

    /**
     * Experience-centric verification (kept for the existing UI).
     * The request body is mandatory and identical to the task endpoint.
     */
    @PatchMapping("/{id}/verify")
    @Operation(
        summary = "Verify an experience (owning SME only)",
        description = "Alias of PATCH /api/v1/tasks/{id}/verify. Requires rating, feedback and "
            + "reference, and atomically verifies the task + experience, creates the payment, "
            + "appends the ledger entry and publishes the event."
    )
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyExperience(
        @PathVariable Long id,
        @Valid @RequestBody VerificationRequest request,
        Authentication authentication) {

        VerificationResponse verified =
            taskService.verifyExperience(id, request, authentication.getName());
        return ResponseEntity.ok(
            ApiResponse.success("Experience verified", verified));
    }
}
