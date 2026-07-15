package com.society.management.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MaintenanceGenerationRequest(
        @NotNull Integer month,
        @NotNull Integer year,
        BigDecimal overrideAmount,
        String dueDate) {}
