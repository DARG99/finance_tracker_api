package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.Transaction;
import com.money.finance_tracker.entity.TransactionNature;
import com.money.finance_tracker.entity.TransactionTypeEnum;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> filteredBy(
            Long userId,
            TransactionTypeEnum type,
            Long categoryId,
            Long fundingSourceId,
            String search,
            LocalDate from,
            LocalDate to,
            TransactionNature transactionNature
    ) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();

            predicate = builder.and(
                    predicate,
                    builder.equal(root.get("user").get("id"), userId)
            );

            if (type != null) {
                predicate = builder.and(
                        predicate,
                        builder.equal(root.get("type"), type)
                );
            }

            if (transactionNature != null) {
                predicate = builder.and(
                        predicate,
                        builder.equal(
                                root.get("transactionNature"),
                                transactionNature
                        )
                );
            }

            if (categoryId != null) {
                predicate = builder.and(
                        predicate,
                        builder.equal(
                                root.get("category").get("id"),
                                categoryId
                        )
                );
            }

            if (fundingSourceId != null) {
                // Either side can be absent for income/expenses; preserve those rows.
                var source = root.join("sourceFundingSource", JoinType.LEFT);
                var destination = root.join("destinationFundingSource", JoinType.LEFT);
                predicate = builder.and(
                        predicate,
                        builder.or(
                                builder.equal(source.get("id"), fundingSourceId),
                                builder.equal(destination.get("id"), fundingSourceId)
                        )
                );
            }

            if (search != null && !search.isBlank()) {
                predicate = builder.and(
                        predicate,
                        builder.like(
                                builder.lower(root.get("description")),
                                "%" + search.trim().toLowerCase() + "%"
                        )
                );
            }

            if (from != null) {
                predicate = builder.and(
                        predicate,
                        builder.greaterThanOrEqualTo(
                                root.get("transactionDate"),
                                from
                        )
                );
            }

            if (to != null) {
                predicate = builder.and(
                        predicate,
                        builder.lessThanOrEqualTo(
                                root.get("transactionDate"),
                                to
                        )
                );
            }

            return predicate;
        };
    }
}
