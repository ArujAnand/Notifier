package com.investmentsentinel.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record InvestmentRecordDto(
        @NotNull Long investmentId,
        @NotNull @DecimalMin("1.00") BigDecimal amount,
        @NotNull LocalDate investmentDate,
        String notes
) {}
