package com.money.finance_tracker.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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

    public void changeName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Funding source name is required");
        this.name = name;
    }

}
