package com.society.management.dto.request;

import com.society.management.entity.OccupancyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FlatRequest(
        @NotNull Long buildingId,
        @NotBlank String flatNumber,
        Integer floor,
        BigDecimal areaSqft,
        String bhk,
        OccupancyStatus occupancy,
        BigDecimal monthlyMaintenance) {}
