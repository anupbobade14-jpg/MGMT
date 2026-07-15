package com.society.management.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BuildingRequest(
        @NotBlank String name,
        String wing,
        String address,
        Integer totalFloors) {}
