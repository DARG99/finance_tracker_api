package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.dto.FundingSourceResponseDto;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FundingSourceService {

    @Autowired
    private FundingSourceRepository fundingSourceRepository;

    @Transactional
    public FundingSourceResponseDto addFundingSource(
            FundingSourceDto dto,
            User user
    ) {
        BigDecimal initialBalance = dto.getInitialBalance() != null
                ? dto.getInitialBalance()
                : BigDecimal.ZERO;

        FundingSource fundingSource = new FundingSource();
        fundingSource.setName(dto.getName());
        fundingSource.setUser(user);
        fundingSource.setInitialBalance(initialBalance);
        fundingSource.setBalance(initialBalance);

        FundingSource savedFundingSource =
                fundingSourceRepository.save(fundingSource);

        return toResponseDto(savedFundingSource);
    }

    @Transactional
    public List<FundingSourceResponseDto> getFundingSources(User user) {
        return fundingSourceRepository
                .findAllByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private FundingSourceResponseDto toResponseDto(
            FundingSource fundingSource
    ) {
        return new FundingSourceResponseDto(
                fundingSource.getId(),
                fundingSource.getName(),
                fundingSource.getBalance()
        );
    }
}
