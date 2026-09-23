package com.money.finance_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "type",
            nullable = false,
            columnDefinition = "transaction_type"
    )
    private TransactionTypeEnum type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // EXPENSE and TRANSFER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_funding_source_id")
    private FundingSource sourceFundingSource;

    // INCOME and TRANSFER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_funding_source_id")
    private FundingSource destinationFundingSource;

    // EXPENSE only
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Always supplied by the user
    @NotNull(message = "Transaction date is required")
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    // Automatically set by the backend
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
