package com.money.finance_tracker.service;

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

    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }
}

