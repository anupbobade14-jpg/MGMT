package com.society.management.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NoticeRequest(
        @NotBlank String title,
        @NotBlank String body,
        String category,
        boolean pinned,
        String expiresAt) {}
