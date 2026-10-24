package com.ssh.controller;

import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.YouthResponse;
import com.ssh.service.YouthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/youth")
@Tag(name = "Youth", description = "Youth profile lookup")
public class YouthController {

    private final YouthService youthService;

    public YouthController(YouthService youthService) {
        this.youthService = youthService;
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get my youth profile (YOUTH only)",
        description = "Returns the youth-table ID required by the experience, application and "
            + "assignment APIs. The users.id is not the youthId."
    )
    public ResponseEntity<ApiResponse<YouthResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            "Youth profile",
            youthService.getMyProfile(authentication.getName())));
    }
}
