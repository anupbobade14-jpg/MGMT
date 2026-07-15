package com.society.management.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentReviewRequest(
        @NotNull Boolean approve,
        String notes) {}
