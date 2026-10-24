package com.ssh.dto;

import com.ssh.dto.request.TaskRequest;
import com.ssh.dto.response.ExperienceResponse;
import com.ssh.dto.response.SMEResponse;
import com.ssh.dto.response.TaskResponse;
import com.ssh.dto.response.UserResponse;
import com.ssh.dto.response.YouthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the JSON names of boolean fields that the Svelte client reads.
 *
 * JavaBean naming would otherwise turn isPaid() into "paid", isVerified()
 * into "verified", and so on. The frontend (and its local mock data) uses the
 * "isX" names, so the API contract must keep them stable.
 */
class BooleanJsonPropertyContractTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    @DisplayName("TaskResponse exposes isPaid, not paid")
    void taskResponseUsesIsPaidProperty() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Task", "OPEN");
        response.setPaid(true);

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"isPaid\":true");
        assertThat(json).doesNotContain("\"paid\":");
    }

    @Test
    @DisplayName("TaskRequest accepts both isPaid and the legacy paid alias")
    void taskRequestAcceptsIsPaidAndPaidAlias() throws Exception {
        TaskRequest current = mapper.readValue(
            "{\"title\":\"Task\",\"category\":\"Digital\",\"isPaid\":false}",
            TaskRequest.class
        );
        TaskRequest legacy = mapper.readValue(
            "{\"title\":\"Task\",\"category\":\"Digital\",\"paid\":false}",
            TaskRequest.class
        );

        assertThat(current.isPaid()).isFalse();
        assertThat(legacy.isPaid()).isFalse();
    }

    @Test
    @DisplayName("ExperienceResponse exposes isVerified, not verified")
    void experienceResponseUsesIsVerifiedProperty() throws Exception {
        ExperienceResponse response = new ExperienceResponse();
        response.setVerified(true);

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"isVerified\":true");
        assertThat(json).doesNotContain("\"verified\":");
    }

    @Test
    @DisplayName("UserResponse exposes isActive, not active")
    void userResponseUsesIsActiveProperty() throws Exception {
        UserResponse response = new UserResponse(1L, "User", "user@example.com", "YOUTH", true);

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"isActive\":true");
        assertThat(json).doesNotContain("\"active\":");
    }

    @Test
    @DisplayName("YouthResponse exposes isAlumni, not alumni")
    void youthResponseUsesIsAlumniProperty() throws Exception {
        YouthResponse response = new YouthResponse(1L, "Youth", "Cape Town", false);

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"isAlumni\":false");
        assertThat(json).doesNotContain("\"alumni\":");
    }

    @Test
    @DisplayName("SMEResponse exposes isVerified, not verified")
    void smeResponseUsesIsVerifiedProperty() throws Exception {
        SMEResponse response = new SMEResponse(1L, "Business", "Technology", true);

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"isVerified\":true");
        assertThat(json).doesNotContain("\"verified\":");
    }
}
