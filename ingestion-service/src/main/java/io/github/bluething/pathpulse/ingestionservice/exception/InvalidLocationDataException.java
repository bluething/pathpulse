package io.github.bluething.pathpulse.ingestionservice.exception;

public class InvalidLocationDataException extends RuntimeException {
    public InvalidLocationDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
