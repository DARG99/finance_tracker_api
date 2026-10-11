package com.money.finance_tracker.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import com.money.finance_tracker.dto.PaydaySettingsDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.UserRepository;
import com.money.finance_tracker.util.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public PaydaySettingsDto getPayday(User principal) {
        User user = userRepository.lockById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        return new PaydaySettingsDto(user.getPaydayDay());
    }

    @Transactional
    public PaydaySettingsDto setPayday(
            PaydaySettingsDto settings, User principal) {
        Integer day = settings.paydayDay();
        if (day == null || day < 1 || day > 31) {
            throw new IllegalArgumentException("Payday must be between 1 and 31");
        }
        User user = userRepository.lockById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        user.setPaydayDay(day);
        return new PaydaySettingsDto(day);
    }

    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }
}

