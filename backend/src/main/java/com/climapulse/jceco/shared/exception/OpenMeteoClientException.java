package com.climapulse.jceco.shared.exception;

public class OpenMeteoClientException extends ClimapulseException {

    public OpenMeteoClientException(String message) {
        super(message);
    }

    public OpenMeteoClientException(String message, Throwable cause) {
        super(message, cause);
    }
}