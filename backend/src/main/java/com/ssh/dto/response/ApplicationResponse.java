package com.ssh.dto.response;

import com.ssh.entity.Application;

import java.time.LocalDateTime;

public record ApplicationResponse(
    Long id,
    Long taskId,
    String taskTitle,
    Long youthId,
    String youthName,
    String status,
    String coverNote,
    LocalDateTime appliedAt,
    LocalDateTime expiresAt,
    LocalDateTime decidedAt
) {

    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getTask() != null ? application.getTask().getId() : null,
            application.getTask() != null ? application.getTask().getTitle() : null,
            application.getYouth() != null ? application.getYouth().getId() : null,
            application.getYouth() != null ? application.getYouth().getUser().getName() : null,
            application.getStatus(),
            application.getCoverNote(),
            application.getCreatedAt(),
            application.getExpiresAt(),
            application.getDecidedAt()
        );
    }
}
