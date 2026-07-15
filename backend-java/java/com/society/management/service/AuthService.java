package com.society.management.service;

import com.society.management.dto.request.LoginRequest;
import com.society.management.dto.request.RegisterRequest;
import com.society.management.dto.response.AuthResponse;
import com.society.management.entity.*;
import com.society.management.exception.ApiException;
import com.society.management.repository.*;
import com.society.management.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final FlatRepository flatRepo;
    private final OwnerRepository ownerRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final LoginHistoryRepository loginHistoryRepo;
    private final com.society.management.config.AppProperties props;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmailIgnoreCase(req.email()))
            throw ApiException.conflict("Email already registered");

        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .phone(req.phone())
                .role(req.role())
                .provider(AuthProvider.LOCAL)
                .active(true)
                .build();
        user = userRepo.save(user);

        // If OWNER role and flatId supplied, create owner record
        if (req.role() == Role.OWNER && req.flatId() != null) {
            Flat flat = flatRepo.findById(req.flatId())
                    .orElseThrow(() -> ApiException.notFound("Flat not found"));
            Owner owner = Owner.builder()
                    .user(user).flat(flat).fullName(req.fullName())
                    .email(req.email()).phone(req.phone())
                    .primaryOwner(true).build();
            ownerRepo.save(owner);
            if (flat.getOccupancy() == OccupancyStatus.VACANT) {
                flat.setOccupancy(OccupancyStatus.OCCUPIED);
                flatRepo.save(flat);
            }
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, HttpServletRequest http) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (Exception ex) {
            recordLogin(null, req.email(), http, false, ex.getMessage());
            throw ApiException.badRequest("Invalid email or password");
        }
        User user = userRepo.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> ApiException.notFound("User not found"));
        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(http.getRemoteAddr());
        userRepo.save(user);
        recordLogin(user, req.email(), http, true, null);
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String access = jwt.generateAccessToken(user.getEmail(),
                Map.of("uid", user.getId(), "role", user.getRole().name(), "name", user.getFullName()));
        String refresh = jwt.generateRefreshToken(user.getEmail());
        return new AuthResponse(
                access, refresh, "Bearer", props.getJwt().getExpirationMs() / 1000,
                new AuthResponse.UserSummary(user.getId(), user.getEmail(),
                        user.getFullName(), user.getRole().name(), user.getProfilePicture()));
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            var claims = jwt.parse(refreshToken);
            if (!"refresh".equals(claims.get("type"))) throw ApiException.badRequest("Invalid token type");
            User user = userRepo.findByEmailIgnoreCase(claims.getSubject())
                    .orElseThrow(() -> ApiException.notFound("User not found"));
            return issueTokens(user);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid refresh token");
        }
    }

    private void recordLogin(User user, String email, HttpServletRequest r, boolean success, String reason) {
        loginHistoryRepo.save(LoginHistory.builder()
                .user(user).email(email)
                .ipAddress(r.getRemoteAddr())
                .userAgent(r.getHeader("User-Agent"))
                .success(success).failureReason(reason).build());
    }
}
