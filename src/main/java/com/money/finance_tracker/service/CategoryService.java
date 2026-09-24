package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.CategoryDto;
import com.money.finance_tracker.dto.CategoryResponseDto;
import com.money.finance_tracker.entity.Category;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.CategoryRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import com.money.finance_tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public void addCategory(CategoryDto categoryDto, User user) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setUser(user);

        categoryRepository.save(category);
    }

    public List<CategoryResponseDto> getCategories(User user) {
        return categoryRepository
                .findAllByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(category -> new CategoryResponseDto(
                        category.getId(),
                        category.getName()
                ))
                .toList();
    }

    @Transactional
    public void deleteCategory(Long categoryId, User user) {
        Category category = categoryRepository
                .findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));

        if (transactionRepository.existsByCategoryIdAndUserId(
                categoryId,
                user.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a category that is used by transactions"
            );
        }

        categoryRepository.delete(category);
    }
}
