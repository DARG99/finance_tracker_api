package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.dto.FundingSourceResponseDto;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import com.money.finance_tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FundingSourceService {

    @Autowired
    private FundingSourceRepository fundingSourceRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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

    @Transactional
    public void deleteFundingSource(
            Long fundingSourceId,
            User user
    ) {
        FundingSource fundingSource = fundingSourceRepository
                .findByIdAndUserId(fundingSourceId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funding source not found"
                ));

        if (fundingSource.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Funding source balance must be zero before deletion"
            );
        }

        if (transactionRepository.existsByFundingSourceIdAndUserId(
                fundingSourceId,
                user.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a funding source that is used by transactions"
            );
        }

        fundingSourceRepository.delete(fundingSource);
    }
}
