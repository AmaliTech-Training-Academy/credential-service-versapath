package com.capstone.exception;

import com.capstone.dto.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    // Not Found exceptions (these will reach REST layer)
    @ExceptionHandler(CapsuleNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCapsuleNotFoundException(CapsuleNotFoundException ex) {
        log.error("Capsule not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Capsule not found"));
    }

    @ExceptionHandler(AtomNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAtomNotFoundException(AtomNotFoundException ex) {
        log.error("Atom not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Atom not found"));
    }

    // Mapping exceptions (may reach REST layer)
    @ExceptionHandler(CapsuleAtomMappingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCapsuleAtomMappingException(CapsuleAtomMappingException ex) {
        log.error("Capsule atom mapping error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "Capsule atom mapping failed"));
    }

    // Badge-related exceptions
    @ExceptionHandler(BadgeProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadgeProcessingException(BadgeProcessingException ex) {
        log.error("Badge processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "Badge processing failed"));
    }

    // Duplicate exceptions (may reach REST layer)
    @ExceptionHandler(DuplicateCapsuleException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateCapsuleException(DuplicateCapsuleException ex) {
        log.error("Duplicate capsule error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate capsule"));
    }

    // Database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation";
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        // Check for specific constraint violations
        if (rootCause != null) {
            if (rootCause.contains("user_id")) {
                message = "User with this ID already exists";
            } else if (rootCause.contains("email")) {
                message = "User with this email already exists";
            } else if (rootCause.contains("username")) {
                message = "User with this username already exists";
            } else if (rootCause.contains("capsule_id")) {
                message = "Capsule with this ID already exists";
            } else if (rootCause.contains("atom_id")) {
                message = "Atom with this ID already exists";
            } else if (rootCause.contains("uk_learner_capsule_badge")) {
                message = "Badge already issued for this learner and capsule";
            } else if (rootCause.contains("verification_code")) {
                message = "Badge with this verification code already exists";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(message, "Data constraint violation"));
    }

    // Validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(errors, "Validation failed"));
    }

    // Illegal argument exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(ex.getMessage(), "Invalid argument"));
    }

    // Generic exception handler (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("An unexpected error occurred", "Internal server error"));
    }
}
