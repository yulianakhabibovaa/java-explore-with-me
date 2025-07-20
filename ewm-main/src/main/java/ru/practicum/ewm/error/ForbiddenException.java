package ru.practicum.ewm.error;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "For the requested operation the conditions are not met");
    }
}
