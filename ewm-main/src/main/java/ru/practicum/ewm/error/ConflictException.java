package ru.practicum.ewm.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "For the requested operation the conditions are not met");
    }
}
