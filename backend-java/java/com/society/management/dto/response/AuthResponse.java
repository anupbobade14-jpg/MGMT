package com.society.management.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserSummary user) {

    public record UserSummary(Long id, String email, String fullName, String role, String profilePicture) {}
}
