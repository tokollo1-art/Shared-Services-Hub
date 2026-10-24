package com.ssh.dto.response;

import com.ssh.entity.Wallet;

import java.time.LocalDateTime;

public record WalletResponse(
    Long id,
    String name,
    Double balance,
    String currency,
    LocalDateTime updatedAt
) {

    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getName(),
            wallet.getBalance(),
            wallet.getCurrency(),
            wallet.getUpdatedAt()
        );
    }
}
