package com.society.management.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VisitorRequest(
        @NotBlank String visitorName,
        String phone,
        String purpose,
        String visitType,
        String vehicleNumber,
        Long flatId) {}
