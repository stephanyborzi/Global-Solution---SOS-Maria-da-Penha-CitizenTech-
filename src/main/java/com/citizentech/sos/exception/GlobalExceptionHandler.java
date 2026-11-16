package com.citizentech.sos.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, "Invalid data");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, ConstraintViolationException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<Map<String, Object>> invalidRequest(Exception ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Invalid request"));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> businessRule(BusinessRuleException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT,
                "Operation violates data integrity (linked or duplicate records)"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> unauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Access denied"));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<Map<String, Object>> tooManyAttempts(TooManyAttemptsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(body(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()));
    }

    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", LocalDateTime.now().toString());
        m.put("status", status.value());
        m.put("error", message);
        return m;
    }
}
