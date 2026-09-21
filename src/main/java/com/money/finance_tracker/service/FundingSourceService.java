package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FundingSourceService {

    @Autowired
    private FundingSourceRepository fundingSourceRepository;

    @Autowired
    private UserRepository userRepository;

    public void addFundingSource(@Valid FundingSourceDto fundingSourceDto) {
        User targetUser = userRepository.findById(fundingSourceDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + fundingSourceDto.getUserId()));

        FundingSource fundingSource = new FundingSource();
        fundingSource.setName(fundingSourceDto.getName());
        fundingSource.setUser(targetUser);

        fundingSourceRepository.save(fundingSource);
    }
}

