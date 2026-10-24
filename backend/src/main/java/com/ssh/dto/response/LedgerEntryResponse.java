package com.ssh.dto.response;

import com.ssh.entity.LedgerEntry;

import java.time.LocalDateTime;

public record LedgerEntryResponse(
    Long id,
    Long sequence,
    Long experienceId,
    Long taskId,
    Long youthId,
    Double rating,
    String feedback,
    String reference,
    LocalDateTime verifiedAt,
    String previousHash,
    String hash
) {

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
            entry.getId(),
            entry.getSequence(),
            entry.getExperienceId(),
            entry.getTaskId(),
            entry.getYouthId(),
            entry.getRating(),
            entry.getFeedback(),
            entry.getReference(),
            entry.getVerifiedAt(),
            entry.getPreviousHash(),
            entry.getHash()
        );
    }
}
