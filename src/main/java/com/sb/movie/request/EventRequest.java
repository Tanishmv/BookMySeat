package com.sb.movie.request;

import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import lombok.Data;

import java.sql.Date;

@Data
public class EventRequest {
    private String name;
    private EventType eventType;
    private Integer duration;
    private Double rating;
    private Date releaseDate;
    private Genre genre;
    private Language language;

    // Event-type specific fields
    private String artist;      // For concerts, comedy shows, dance shows
    private String director;    // For movies, theater
    private String performers;  // For movies, theater, opera
    private String description;
    private String posterUrl;
}
