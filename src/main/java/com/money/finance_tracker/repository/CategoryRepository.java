package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndUserId(Long categoryId, Long id);
}
