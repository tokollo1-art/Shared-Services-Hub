package com.ssh.dto.response;

/**
 * Result of re-walking the hash chain. Publicly readable so anyone can check
 * that the Experience Ledger has not been altered.
 */
public record LedgerVerificationResponse(
    boolean valid,
    int checkedEntries,
    Long brokenAtSequence,
    String message
) {

    public static LedgerVerificationResponse valid(int checkedEntries) {
        return new LedgerVerificationResponse(
            true,
            checkedEntries,
            null,
            "Ledger chain is intact (" + checkedEntries + " entries verified)"
        );
    }

    public static LedgerVerificationResponse invalid(int checkedEntries, Long brokenAtSequence,
                                                     String message) {
        return new LedgerVerificationResponse(false, checkedEntries, brokenAtSequence, message);
    }
}
