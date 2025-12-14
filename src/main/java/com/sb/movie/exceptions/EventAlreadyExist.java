package com.sb.movie.exceptions;

public class EventAlreadyExist extends RuntimeException {
    public EventAlreadyExist() {
        super("Event Already Exists in the Database");
    }

    public EventAlreadyExist(String message) {
        super(message);
    }
}
