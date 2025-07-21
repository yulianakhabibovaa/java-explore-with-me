package ru.practicum.ewm.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .toList();

        return new ApiError(
                errors,
                "Incorrectly made request",
                "Validation failed",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBindException(BindException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Parameter '%s': %s", error.getField(), error.getDefaultMessage()))
                .toList();

        return new ApiError(
                errors,
                "Invalid request parameters",
                "Parameter validation failed",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParams(MissingServletRequestParameterException ex) {
        String error = String.format("Parameter '%s' is required", ex.getParameterName());
        return new ApiError(
                Collections.singletonList(error),
                "Missing required parameter",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> String.format("Field: %s. Error: %s. Value: %s",
                        violation.getPropertyPath(), violation.getMessage(), violation.getInvalidValue()))
                .toList();

        return new ApiError(
                errors,
                HttpStatus.CONFLICT.name(),
                "Integrity constraint has been violated",
                "Database constraint violation",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("Parameter '%s' must be of type %s",
                ex.getName(), ex.getRequiredType());
        return new ApiError(
                Collections.singletonList(error),
                "Type mismatch",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiError handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return new ApiError(
                Collections.emptyList(),
                "Method not allowed",
                ex.getMessage(),
                HttpStatus.METHOD_NOT_ALLOWED.name(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return new ApiError(
                Collections.emptyList(),
                "CONFLICT",
                "Integrity constraint has been violated",
                ex.getMessage(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex) {
        ApiError apiError = new ApiError(
                Collections.emptyList(),
                ex.getStatus().name(),
                ex.getReason(),
                ex.getMessage(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
        return new ResponseEntity<>(apiError, ex.getStatus());
    }
}
