package com.ssh.controller;

import com.ssh.dto.request.ApplicationRequest;
import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.ApplicationResponse;
import com.ssh.service.ApplicationService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Applications", description = "Youth applications, reservations and tier checks")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/tasks/{taskId}/apply")
    @Operation(
        summary = "Apply for an open task (YOUTH only)",
        description = "Creates a 48-hour reservation. A duplicate PENDING/ACCEPTED application "
            + "returns 409; REJECTED, WITHDRAWN and EXPIRED applications are reactivated. "
            + "The tier ceiling is enforced before the reservation is created."
    )
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
        @PathVariable Long taskId,
        @Valid @RequestBody(required = false) ApplicationRequest request,
        Authentication authentication) {

        ApplicationResponse application =
            applicationService.apply(taskId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Application submitted", application));
    }

    @GetMapping("/applications/mine")
    @Operation(summary = "List my applications (YOUTH only)")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> mine(Authentication authentication) {
        List<ApplicationResponse> applications =
            applicationService.listMine(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("My applications", applications));
    }

    @GetMapping("/tasks/{taskId}/applications")
    @Operation(summary = "List applications for a task (owning SME only)")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> forTask(
        @PathVariable Long taskId,
        Authentication authentication) {

        List<ApplicationResponse> applications =
            applicationService.listForTask(taskId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Task applications", applications));
    }

    @PatchMapping("/applications/{id}/withdraw")
    @Operation(summary = "Withdraw a pending application (applicant only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdraw(
        @PathVariable Long id,
        Authentication authentication) {

        ApplicationResponse application =
            applicationService.withdraw(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn", application));
    }
}
