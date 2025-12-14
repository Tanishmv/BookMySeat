package com.sb.movie.exceptions;

public class EventDoesNotExist extends RuntimeException {
    public EventDoesNotExist() {
        super("Event Does Not Exist in the Database");
    }

    public EventDoesNotExist(String message) {
        super(message);
    }
}
