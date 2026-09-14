package com.projectmanagement.app.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;

/**
 * Converts server-side failures into concise, actionable messages for the UI.
 *
 * Technical exception messages and stack traces are deliberately never returned
 * to clients. Full details are logged on the server for diagnosis.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException exception) {
        log.debug("Authentication failed", exception);
        return buildResponse(HttpStatus.UNAUTHORIZED,
                "The email or password is incorrect. Please check your details and try again.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException exception) {
        log.debug("Access denied", exception);
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        log.warn("Request failed with status {}", status.value(), exception);

        String message = sanitizeMessage(exception.getReason());
        if (message == null) {
            message = defaultMessage(status);
        }

        return buildResponse(status, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
        log.debug("Request validation failed", exception);

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(
                humanizeFieldName(error.getField()),
                safeValidationMessage(error.getDefaultMessage())));

        Map<String, Object> response = baseResponse(
                HttpStatus.BAD_REQUEST,
                errors.isEmpty()
                        ? "Please check the information you entered and try again."
                        : "Please correct the highlighted fields.");

        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception) {
        log.debug("Constraint validation failed", exception);

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> errors.putIfAbsent(
                humanizeFieldName(violation.getPropertyPath().toString()),
                safeValidationMessage(violation.getMessage())));

        Map<String, Object> response = baseResponse(
                HttpStatus.BAD_REQUEST,
                "Please check the information entered and try again.");

        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        log.warn("Request body could not be read", exception);
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Some of the information could not be read. Please review your entries and try again.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        log.debug("Required request parameter is missing: {}", exception.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Some required information is missing. Please complete all required fields and try again.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.debug("Request parameter has an invalid value: {}", exception.getName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "One of the values entered is not valid. Please review it and try again.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.error("Data integrity error", exception);
        return buildResponse(HttpStatus.CONFLICT,
                "This change could not be saved because it conflicts with existing information. Please review the values and try again.");
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException exception) {
        log.error("Unexpected null value while processing a request", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "We could not complete this request because some information was unavailable. Please refresh the page and try again.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Invalid argument supplied", exception);
        return buildResponse(HttpStatus.BAD_REQUEST,
                "One or more values are not valid. Please review your entries and try again.");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException exception) {
        log.warn("Request could not be completed in the current state", exception);
        return buildResponse(HttpStatus.CONFLICT,
                "This action cannot be completed with the current information. Please refresh and try again.");
    }

    /**
     * RuntimeException is intentionally handled without returning
     * exception.getMessage()
     * directly because service/database messages may contain implementation
     * details.
     * Simple business messages such as "Project not found" remain useful to the
     * user.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException exception) {
        log.error("Unexpected application error", exception);

        String message = sanitizeMessage(exception.getMessage());
        return buildResponse(
                message == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST,
                message == null
                        ? "We could not complete your request. Please check your information and try again."
                        : message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception) {
        log.error("Unhandled application error", exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong while processing your request. Please try again. If the problem continues, contact your administrator.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseResponse(status, message));
    }

    private Map<String, Object> baseResponse(HttpStatus status, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return response;
    }

    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        String cleaned = message.trim().replaceAll("\\s+", " ");

        if (cleaned.length() > 300
                || containsTechnicalDetail(cleaned)) {
            return null;
        }

        return cleaned;
    }

    private boolean containsTechnicalDetail(String message) {
        String value = message.toLowerCase();

        return value.contains("nullpointerexception")
                || value.contains("java.")
                || value.contains("org.springframework")
                || value.contains("hibernate")
                || value.contains("sql")
                || value.contains("jdbc")
                || value.contains("stack trace")
                || value.contains(" at com.")
                || value.contains("constraint [")
                || value.contains("could not execute")
                || value.contains("database error")
                || value.contains("syntax error")
                || value.contains("psql");
    }

    private String safeValidationMessage(String message) {
        String cleaned = sanitizeMessage(message);
        return cleaned == null
                ? "Please enter a valid value."
                : cleaned;
    }

    private String humanizeFieldName(String field) {
        if (field == null || field.isBlank()) {
            return "Field";
        }

        String name = field.substring(field.lastIndexOf('.') + 1)
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .trim();

        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String defaultMessage(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Please check the information entered and try again.";
            case UNAUTHORIZED -> "Your session has expired. Please sign in again.";
            case FORBIDDEN -> "You do not have permission to perform this action.";
            case NOT_FOUND -> "The requested information could not be found.";
            case CONFLICT -> "This change conflicts with existing information. Please review it and try again.";
            case TOO_MANY_REQUESTS -> "Too many requests were made. Please wait a moment and try again.";
            default -> "Something went wrong while processing your request. Please try again.";
        };
    }
}
