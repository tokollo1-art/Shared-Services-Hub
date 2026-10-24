package com.ssh.service;

import com.ssh.dto.response.WalletResponse;
import com.ssh.dto.response.WalletTransactionResponse;
import com.ssh.entity.User;
import com.ssh.entity.Wallet;
import com.ssh.entity.WalletTransaction;
import com.ssh.exception.InsufficientFundsException;
import com.ssh.repository.WalletRepository;
import com.ssh.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sandbox funder wallet.
 *
 * The demo uses a single reserve wallet funded by corporate pledges. Payouts
 * debit that wallet; SMEs never touch the money.
 */
@Service
public class WalletService {

    public static final String RESERVE_WALLET_NAME = "SSH_PLATFORM_RESERVE";
    public static final double INITIAL_SANDBOX_BALANCE = 100_000.0;

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserAccountService userAccountService;

    public WalletService(WalletRepository walletRepository,
                         WalletTransactionRepository walletTransactionRepository,
                         UserAccountService userAccountService) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.userAccountService = userAccountService;
    }

    /**
     * A funder or admin can view the shared reserve wallet.
     */
    @Transactional
    public WalletResponse getReserve(String email) {
        requireFunderOrAdmin(email);
        return WalletResponse.from(getOrCreateReserveWallet());
    }

    /**
     * Sandbox top-up: corporate funders (or an admin) add money to the reserve.
     */
    @Transactional
    public WalletResponse fund(double amount, String reference, String email) {
        requireFunderOrAdmin(email);
        if (amount <= 0) {
            throw new IllegalArgumentException("Funding amount must be positive");
        }

        Wallet wallet = getOrCreateReserveWallet();
        wallet.credit(amount);
        Wallet saved = walletRepository.save(wallet);
        recordTransaction(saved, "CREDIT", amount, reference);

        return WalletResponse.from(saved);
    }

    @Transactional
    public List<WalletTransactionResponse> listTransactions(String email) {
        requireFunderOrAdmin(email);
        Wallet wallet = getOrCreateReserveWallet();
        return walletTransactionRepository.findByWalletOrderByCreatedAtDesc(wallet)
            .stream()
            .map(WalletTransactionResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * Debits the reserve for a sandbox payout. Synchronised because the wallet
     * balance is a single row and the demo has no pessimistic-lock plumbing.
     */
    @Transactional
    public synchronized void debitReserve(double amount, String reference) {
        Wallet wallet = getOrCreateReserveWallet();
        if (wallet.getBalance() < amount) {
            throw new InsufficientFundsException(
                "Funder reserve has R" + wallet.getBalance()
                    + " but the payout needs R" + amount
                    + ". Top up the wallet and retry.");
        }
        wallet.debit(amount);
        Wallet saved = walletRepository.save(wallet);
        recordTransaction(saved, "DEBIT", amount, reference);
    }

    @Transactional
    public synchronized Wallet getOrCreateReserveWallet() {
        return walletRepository.findByName(RESERVE_WALLET_NAME)
            .orElseGet(() -> walletRepository.save(
                new Wallet(RESERVE_WALLET_NAME, INITIAL_SANDBOX_BALANCE)));
    }

    private void recordTransaction(Wallet wallet, String type, double amount, String reference) {
        walletTransactionRepository.save(new WalletTransaction(
            wallet,
            type,
            round(amount),
            wallet.getBalance(),
            reference
        ));
    }

    private void requireFunderOrAdmin(String email) {
        User user = userAccountService.requireUser(email);
        userAccountService.requireAnyRole(user, "CORPORATE", "ADMIN");
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
