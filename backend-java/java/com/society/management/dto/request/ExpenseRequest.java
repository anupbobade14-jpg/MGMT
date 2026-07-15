package com.society.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ExpenseRequest(
        @NotNull Long categoryId,
        @NotNull BigDecimal amount,
        @NotBlank String expenseDate,
        String vendor,
        String description,
        String invoiceNo,
        String paymentMode) {}
