package com.ssh.service;

import com.ssh.dto.response.PaymentResponse;
import com.ssh.entity.Payment;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.event.PaymentPaidEvent;
import com.ssh.exception.BusinessRuleException;
import com.ssh.exception.DuplicateResourceException;
import com.ssh.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String ADMIN_EMAIL = "admin@ssh.co.za";

    @Mock private PaymentRepository paymentRepository;
    @Mock private WalletService walletService;
    @Mock private UserAccountService userAccountService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private PaymentService paymentService;
    private Task task;
    private Youth youth;
    private User admin;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
            paymentRepository, walletService, userAccountService, eventPublisher);

        User youthUser = new User("Sipho", "sipho@ssh.co.za", "hash", "YOUTH");
        youthUser.setId(3L);
        youth = new Youth(youthUser);
        youth.setId(30L);

        User smeUser = new User("Thandi", "thandi@ssh.co.za", "hash", "SME");
        SME sme = new SME(smeUser, "Thandi's Accounting");
        sme.setId(10L);
        task = new Task("Design a logo", "Brand identity", "DESIGN", sme);
        task.setId(100L);
        task.setBudget(1000.0);

        admin = new User("Admin", ADMIN_EMAIL, "hash", "ADMIN");
        admin.setId(1L);
    }

    @Test
    @DisplayName("Verification creates one payment with a 10% platform fee")
    void shouldCreatePayment_WithBreakdown() {
        when(paymentRepository.findByTask(task)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.createForVerifiedTask(task, youth);

        assertThat(payment.getTotalAmount()).isEqualTo(1000.0);
        assertThat(payment.getPlatformFee()).isEqualTo(100.0);
        assertThat(payment.getYouthAmount()).isEqualTo(900.0);
        assertThat(payment.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("A missing task budget produces a zero-value payment")
    void shouldCreateZeroPayment_WhenBudgetIsUnset() {
        task.setBudget(null);
        when(paymentRepository.findByTask(task)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.createForVerifiedTask(task, youth);

        assertThat(payment.getTotalAmount()).isZero();
        assertThat(payment.getPlatformFee()).isZero();
        assertThat(payment.getYouthAmount()).isZero();
    }

    @Test
    @DisplayName("A task cannot have a second payment")
    void shouldRejectSecondPayment_ForTheSameTask() {
        when(paymentRepository.findByTask(task))
            .thenReturn(Optional.of(new Payment(task, youth, 1000.0, 10.0)));

        assertThatThrownBy(() -> paymentService.createForVerifiedTask(task, youth))
            .isInstanceOf(DuplicateResourceException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("An admin payout debits the reserve, marks PAID and publishes an event")
    void shouldPayoutFromReserve() {
        Payment payment = new Payment(task, youth, 1000.0, 10.0);
        payment.setId(600L);

        when(userAccountService.requireUser(ADMIN_EMAIL)).thenReturn(admin);
        when(paymentRepository.findById(600L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.payout(600L, ADMIN_EMAIL);

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.reference()).startsWith("SANDBOX-");
        assertThat(response.paidAt()).isNotNull();
        verify(walletService).debitReserve(eq(900.0), any());

        ArgumentCaptor<PaymentPaidEvent> eventCaptor =
            ArgumentCaptor.forClass(PaymentPaidEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().paymentId()).isEqualTo(600L);
        assertThat(eventCaptor.getValue().amount()).isEqualTo(900.0);
    }

    @Test
    @DisplayName("A non-admin cannot release money")
    void shouldDenyPayout_ToNonAdmin() {
        when(userAccountService.requireUser("sme@ssh.co.za"))
            .thenReturn(new User("SME", "sme@ssh.co.za", "hash", "SME"));
        doThrow(new AccessDeniedException("Requires ADMIN"))
            .when(userAccountService).requireAnyRole(any(User.class), any(String[].class));

        assertThatThrownBy(() -> paymentService.payout(600L, "sme@ssh.co.za"))
            .isInstanceOf(AccessDeniedException.class);

        verify(walletService, never()).debitReserve(anyDouble(), anyString());
    }

    @Test
    @DisplayName("A payment cannot be paid twice")
    void shouldRejectSecondPayout() {
        Payment payment = new Payment(task, youth, 1000.0, 10.0);
        payment.setId(600L);
        payment.markAsPaid("SANDBOX-existing");

        when(userAccountService.requireUser(ADMIN_EMAIL)).thenReturn(admin);
        when(paymentRepository.findById(600L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.payout(600L, ADMIN_EMAIL))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("already");

        verify(walletService, never()).debitReserve(anyDouble(), anyString());
    }

    @Test
    @DisplayName("A zero-value payment is marked PAID without debiting the reserve")
    void shouldNotDebitReserve_ForZeroPayment() {
        task.setBudget(null);
        Payment payment = new Payment(task, youth, 0.0, 10.0);
        payment.setId(600L);

        when(userAccountService.requireUser(ADMIN_EMAIL)).thenReturn(admin);
        when(paymentRepository.findById(600L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.payout(600L, ADMIN_EMAIL);

        assertThat(response.status()).isEqualTo("PAID");
        verify(walletService, never()).debitReserve(anyDouble(), anyString());
    }
}
