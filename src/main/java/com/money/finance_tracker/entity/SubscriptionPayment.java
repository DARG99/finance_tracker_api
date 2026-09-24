package com.money.finance_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "subscription_payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "subscription_payment_once_per_due_date",
                        columnNames = {"subscription_id", "scheduled_for"}
                ),
                @UniqueConstraint(
                        name = "subscription_payment_one_transaction",
                        columnNames = "transaction_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDate scheduledFor;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}