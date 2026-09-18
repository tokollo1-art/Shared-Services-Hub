package com.ssh.controller;

import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.ExperienceResponse;
import com.ssh.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Experience Controller
 *
 * Purpose: REST endpoints for the Experience Ledger.
 *
 * WHY SSH needs this controller:
 *   - Youth view their verified experience through GET endpoints
 *   - SMEs verify experiences through PATCH endpoints
 *   - Powers the "verified work record" moment in the demo
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/experiences")
@Tag(name = "Experiences", description = "The Experience Ledger — verified work records")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    /**
     * Get a single experience by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an experience by ID")
    public ResponseEntity<ApiResponse<ExperienceResponse>> getExperience(
        @PathVariable Long id) {

        ExperienceResponse experience = experienceService.getExperienceById(id);
        return ResponseEntity.ok(ApiResponse.success("Experience found", experience));
    }

    /**
     * List all experiences for a given youth.
     * This is the youth's Experience Ledger.
     */
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
     * Verify an experience. Called by the SME after reviewing the completed work.
     * Optional query params: feedback, rating.
     */
    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verify an experience (SME only)")
    public ResponseEntity<ApiResponse<ExperienceResponse>> verifyExperience(
        @PathVariable Long id,
        @RequestParam(required = false) String feedback,
        @RequestParam(required = false) Double rating) {

        ExperienceResponse verified =
            experienceService.verifyExperience(id, feedback, rating);
        return ResponseEntity.ok(
            ApiResponse.success("Experience verified", verified));
    }
}
