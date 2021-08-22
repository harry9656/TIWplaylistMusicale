package com.harry9656.MusicalSpace.exceptions;

public class InvalidPlaylistDataException extends RuntimeException {
    public InvalidPlaylistDataException(String message) {
        super(message);
    }

    public InvalidPlaylistDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
