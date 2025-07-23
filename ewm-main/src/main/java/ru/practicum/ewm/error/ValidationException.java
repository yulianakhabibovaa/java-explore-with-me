package ru.practicum.ewm.error;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Incorrectly made request");
    }
}
