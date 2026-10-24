package com.ssh.service;

import com.ssh.dto.response.WalletResponse;
import com.ssh.entity.User;
import com.ssh.entity.Wallet;
import com.ssh.entity.WalletTransaction;
import com.ssh.exception.InsufficientFundsException;
import com.ssh.repository.WalletRepository;
import com.ssh.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    private static final String CORP_EMAIL = "funder@corp.co.za";

    @Mock private WalletRepository walletRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private UserAccountService userAccountService;

    private WalletService walletService;
    private User corporateUser;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(
            walletRepository, walletTransactionRepository, userAccountService);

        corporateUser = new User("Funder", CORP_EMAIL, "hash", "CORPORATE");
        corporateUser.setId(9L);
    }

    @Test
    @DisplayName("The reserve wallet is created with the sandbox balance")
    void shouldCreateReserveWallet_WithSeedBalance() {
        when(userAccountService.requireUser(CORP_EMAIL)).thenReturn(corporateUser);
        when(walletRepository.findByName(WalletService.RESERVE_WALLET_NAME))
            .thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.getReserve(CORP_EMAIL);

        assertThat(response.name()).isEqualTo(WalletService.RESERVE_WALLET_NAME);
        assertThat(response.balance()).isEqualTo(WalletService.INITIAL_SANDBOX_BALANCE);
        assertThat(response.currency()).isEqualTo("ZAR");
    }

    @Test
    @DisplayName("Funding credits the reserve and records a CREDIT transaction")
    void shouldFundReserve_AndRecordTransaction() {
        Wallet wallet = new Wallet(WalletService.RESERVE_WALLET_NAME, 1000.0);
        wallet.setId(1L);
        when(userAccountService.requireUser(CORP_EMAIL)).thenReturn(corporateUser);
        when(walletRepository.findByName(WalletService.RESERVE_WALLET_NAME))
            .thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        WalletResponse response = walletService.fund(2500.0, "CORP-PLEDGE-1", CORP_EMAIL);

        assertThat(response.balance()).isEqualTo(3500.0);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("A payout debit reduces the reserve and records a DEBIT transaction")
    void shouldDebitReserve_AndRecordTransaction() {
        Wallet wallet = new Wallet(WalletService.RESERVE_WALLET_NAME, 1000.0);
        wallet.setId(1L);
        when(walletRepository.findByName(WalletService.RESERVE_WALLET_NAME))
            .thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.debitReserve(250.0, "Payout for payment 1");

        assertThat(wallet.getBalance()).isEqualTo(750.0);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("A payout larger than the reserve is rejected")
    void shouldRejectPayout_WhenReserveIsTooLow() {
        Wallet wallet = new Wallet(WalletService.RESERVE_WALLET_NAME, 100.0);
        wallet.setId(1L);
        when(walletRepository.findByName(WalletService.RESERVE_WALLET_NAME))
            .thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.debitReserve(500.0, "Payout"))
            .isInstanceOf(InsufficientFundsException.class)
            .hasMessageContaining("Top up");

        assertThat(wallet.getBalance()).isEqualTo(100.0);
        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("A youth cannot read the funder wallet")
    void shouldDenyWalletAccess_ToYouth() {
        User youth = new User("Sipho", "sipho@ssh.co.za", "hash", "YOUTH");
        when(userAccountService.requireUser("sipho@ssh.co.za")).thenReturn(youth);
        org.mockito.Mockito.doThrow(new AccessDeniedException("Requires ADMIN or CORPORATE"))
            .when(userAccountService).requireAnyRole(any(User.class), any(String[].class));

        assertThatThrownBy(() -> walletService.getReserve("sipho@ssh.co.za"))
            .isInstanceOf(AccessDeniedException.class);
    }
}
