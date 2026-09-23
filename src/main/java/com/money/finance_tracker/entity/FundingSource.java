package com.money.finance_tracker.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "funding_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundingSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Funding source name is required")
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    @JsonBackReference
    private User user;

    @Column(name = "initial_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    public void addToBalance(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void subtractFromBalance(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        balance = balance.subtract(amount);
    }

    public void changeName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Funding source name is required");
        this.name = name;
    }

}
