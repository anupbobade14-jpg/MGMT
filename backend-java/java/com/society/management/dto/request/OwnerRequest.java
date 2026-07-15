package com.society.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerRequest(
        @NotBlank String fullName,
        @Email String email,
        String phone,
        String alternatePhone,
        String emergencyContact,
        Long flatId,
        Long userId,
        String moveInDate) {}
