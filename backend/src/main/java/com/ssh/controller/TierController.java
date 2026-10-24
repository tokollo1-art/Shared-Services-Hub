package com.ssh.controller;

import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.TierInfoResponse;
import com.ssh.entity.Youth;
import com.ssh.service.TierEligibilityService;
import com.ssh.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tiers")
@Tag(name = "Tiers", description = "Verified-experience tier and reservation limits")
public class TierController {

    private final TierEligibilityService tierEligibilityService;
    private final UserAccountService userAccountService;

    public TierController(TierEligibilityService tierEligibilityService,
                          UserAccountService userAccountService) {
        this.tierEligibilityService = tierEligibilityService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get my tier and reservation usage (YOUTH only)",
        description = "Tier 0: 0 verified experiences / 1 reservation. Tier 1: 1-2 / 2. "
            + "Tier 2: 3-4 / 3. Tier 3: 5+ / unlimited. Verified experiences are the only input."
    )
    public ResponseEntity<ApiResponse<TierInfoResponse>> myTier(Authentication authentication) {
        Youth youth = userAccountService.requireYouth(authentication.getName());
        TierInfoResponse info = tierEligibilityService.getTierInfo(youth);
        return ResponseEntity.ok(ApiResponse.success("Tier eligibility", info));
    }
}
