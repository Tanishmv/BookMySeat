package com.sb.movie.exceptions;

public class ShowAlreadyExistsException extends RuntimeException {
    public ShowAlreadyExistsException() {
        super("A show already exists at this theater on the same date and time");
    }

    public ShowAlreadyExistsException(String message) {
        super(message);
    }
}
