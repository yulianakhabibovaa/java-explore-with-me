package ru.practicum.ewm.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllExceptions(Exception ex) {
        return new ApiError(
                Collections.emptyList(),
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "Internal server error",
                ex.getMessage(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }
}
