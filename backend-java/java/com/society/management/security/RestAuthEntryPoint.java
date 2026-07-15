package com.society.management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(jakarta.servlet.http.HttpServletRequest req,
                         HttpServletResponse res,
                         AuthenticationException e) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        mapper.writeValue(res.getWriter(), Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", 401,
                "error", "Unauthorized",
                "message", e.getMessage()
        ));
    }
}
