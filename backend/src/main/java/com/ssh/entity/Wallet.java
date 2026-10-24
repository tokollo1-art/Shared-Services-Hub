package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Funder / platform wallet used by the sandbox payout flow.
 *
 * The demo seeds a single platform reserve wallet. Corporate funders top it
 * up; verified payments are paid out from it. This keeps money out of SME
 * hands, matching the SSH model ("SMEs never handle money").
 */
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Wallet name is required")
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @PositiveOrZero(message = "Balance must be positive or zero")
    @Column(nullable = false)
    private Double balance = 0.0;

    @NotBlank
    @Size(max = 3)
    @Column(nullable = false, length = 3)
    private String currency = "ZAR";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Wallet() {
    }

    public Wallet(String name, Double balance) {
        this.name = name;
        this.balance = balance != null ? balance : 0.0;
        this.currency = "ZAR";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void credit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = round(this.balance + amount);
    }

    public void debit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("Insufficient wallet balance");
        }
        this.balance = round(this.balance - amount);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Wallet{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", balance=" + balance +
            ", currency='" + currency + '\'' +
            '}';
    }
}
