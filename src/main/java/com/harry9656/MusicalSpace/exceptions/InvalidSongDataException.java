package com.harry9656.MusicalSpace.exceptions;

public class InvalidSongDataException extends RuntimeException {
    public InvalidSongDataException(String message) {
        super(message);
    }

    public InvalidSongDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
