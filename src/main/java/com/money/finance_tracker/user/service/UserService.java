package com.money.finance_tracker.user.service;

import com.money.finance_tracker.user.dto.FundingSourceDto;
import com.money.finance_tracker.user.dto.UserDto;
import com.money.finance_tracker.user.entity.FundingSource;
import com.money.finance_tracker.user.repository.FundingSourceRepository;
import com.money.finance_tracker.user.repository.UserRepository;
import com.money.finance_tracker.user.dto.UserSignUpDto;
import com.money.finance_tracker.user.entity.User;
import com.money.finance_tracker.user.util.PasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FundingSourceRepository fundingSourceRepository;

    public UserDto createUser(@Valid UserSignUpDto signUpDto) {

        String hashedPassword = passwordService.hashPassword(signUpDto.getPassword());

        User user = new User();
        user.setName(signUpDto.getName());
        user.setEmail(signUpDto.getEmail());
        user.setPasswordHash(hashedPassword);

        User savedUser = userRepository.save(user);

        return new UserDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    public FundingSource addFundingSource(@Valid FundingSourceDto fundingSourceDto) {
        User user = userRepository.findById(fundingSourceDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + fundingSourceDto.getUserId()));

        FundingSource fundingSource = new FundingSource();
        fundingSource.setName(fundingSourceDto.getName());
        fundingSource.setUser(user);

        return fundingSourceRepository.save(fundingSource);
    }
}
