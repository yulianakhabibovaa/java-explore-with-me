package ru.practicum.ewm.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public ApiException(String message, HttpStatus status, String reason) {
        super(message);
        this.status = status;
        this.reason = reason;
    }
}
