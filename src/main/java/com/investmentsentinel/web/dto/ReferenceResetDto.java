package com.investmentsentinel.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReferenceResetDto(
        @NotNull Long investmentId,
        @NotNull @DecimalMin("0.01") BigDecimal newReferenceValue,
        @NotNull LocalDate referenceDate,
        @NotBlank String reason
) {}
