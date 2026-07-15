package com.society.management.security;

import com.society.management.config.AppProperties;
import com.society.management.entity.AuthProvider;
import com.society.management.entity.Role;
import com.society.management.entity.User;
import com.society.management.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwt;
    private final AppProperties props;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) auth.getPrincipal();
        String email = (String) principal.getAttributes().get("email");
        String name  = (String) principal.getAttributes().get("name");
        String sub   = (String) principal.getAttributes().get("sub");
        String picture = (String) principal.getAttributes().get("picture");

        if (email == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by Google");
            return;
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> User.builder()
                .email(email)
                .fullName(name != null ? name : email)
                .role(Role.OWNER)      // default role for new Google sign-ups
                .provider(AuthProvider.GOOGLE)
                .providerId(sub)
                .profilePicture(picture)
                .active(true)
                .build());

        if (user.getProvider() == AuthProvider.LOCAL) {
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(sub);
        }
        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(req.getRemoteAddr());
        user = userRepository.save(user);

        String token = jwt.generateAccessToken(user.getEmail(),
                Map.of("uid", user.getId(), "role", user.getRole().name(), "name", user.getFullName()));

        String redirect = props.getFrontend().getBaseUrl() + "/oauth2/callback?token=" + token;
        res.sendRedirect(redirect);
    }
}
