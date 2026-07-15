package com.society.management.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBad(BadCredentialsException ex) {
        return build(401, "Invalid email or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
        return build(403, "Access denied");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDb(DataIntegrityViolationException ex) {
        log.warn("DB integrity violation", ex);
        return build(409, "Conflict / duplicate data");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a, b) -> a));
        Map<String, Object> body = baseBody(400, "Validation failed");
        body.put("fields", fields);
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("Unhandled error", ex);
        return build(500, "Internal server error");
    }

    private ResponseEntity<Map<String, Object>> build(int status, String msg) {
        return ResponseEntity.status(status).body(baseBody(status, msg));
    }
    private Map<String, Object> baseBody(int status, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("timestamp", OffsetDateTime.now().toString());
        m.put("status", status);
        m.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        m.put("message", msg);
        return m;
    }
}
