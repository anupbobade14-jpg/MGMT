package com.society.management.dto.request;

import com.society.management.entity.PaymentMode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentSubmitRequest(
        @NotNull Long maintenanceId,
        @NotNull BigDecimal amount,
        @NotNull PaymentMode paymentMode,
        String transactionRef,
        String paymentDate) {}
