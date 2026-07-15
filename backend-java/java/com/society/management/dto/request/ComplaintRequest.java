package com.society.management.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ComplaintRequest(
        @NotBlank String subject,
        String description,
        String category,
        String priority,
        Long flatId) {}
