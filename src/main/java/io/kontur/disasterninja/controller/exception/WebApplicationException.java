package io.kontur.disasterninja.controller.exception;

import org.springframework.http.HttpStatus;

public class WebApplicationException extends RuntimeException {

    private final HttpStatus status;
    public final String message;

    public WebApplicationException(String message, HttpStatus status) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
