package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.CategoryDto;
import com.money.finance_tracker.dto.CategoryResponseDto;
import com.money.finance_tracker.entity.Category;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.CategoryRepository;
import com.money.finance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
}
