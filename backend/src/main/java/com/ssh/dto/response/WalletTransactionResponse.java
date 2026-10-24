package com.ssh.dto.response;

import com.ssh.entity.WalletTransaction;

import java.time.LocalDateTime;

public record WalletTransactionResponse(
    Long id,
    String type,
    Double amount,
    Double balanceAfter,
    String reference,
    LocalDateTime createdAt
) {

    public static WalletTransactionResponse from(WalletTransaction transaction) {
        return new WalletTransactionResponse(
            transaction.getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getBalanceAfter(),
            transaction.getReference(),
            transaction.getCreatedAt()
        );
    }
}
