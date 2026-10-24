package com.ssh.service;

import com.ssh.dto.response.YouthResponse;
import com.ssh.entity.Youth;
import org.springframework.stereotype.Service;

/**
 * Read-side youth profile lookups used by the dashboard.
 */
@Service
public class YouthService {

    private final UserAccountService userAccountService;

    public YouthService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * The logged-in youth's profile, including the youth-table ID that the
     * experience and assignment APIs use.
     */
    public YouthResponse getMyProfile(String email) {
        Youth youth = userAccountService.requireYouth(email);

        YouthResponse response = new YouthResponse();
        response.setId(youth.getId());
        response.setUserId(youth.getUser().getId());
        response.setUserName(youth.getUser().getName());
        response.setEducation(youth.getEducation());
        response.setSkills(youth.getSkills());
        response.setPortfolio(youth.getPortfolio());
        response.setBio(youth.getBio());
        response.setLocation(youth.getLocation());
        response.setAvailability(youth.getAvailability());
        response.setAlumni(youth.isAlumni());
        response.setAlumniDate(youth.getAlumniDate());
        response.setCreatedAt(youth.getCreatedAt());
        response.setUpdatedAt(youth.getUpdatedAt());
        return response;
    }
}
