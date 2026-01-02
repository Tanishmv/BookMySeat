package com.sb.movie.services;

import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.request.EventRequest;
import com.sb.movie.request.EventUpdateRequest;

import java.sql.Date;
import java.util.List;

public interface EventService {
    Event addEvent(EventRequest eventRequest);

    Event getEventById(Integer eventId);

    List<Event> searchEvents(String name, String city, EventType eventType, Genre genre, Language language, Date showDate, Date releaseDate);

    Event updateEvent(Integer eventId, EventUpdateRequest eventUpdateRequest);

    String deleteEvent(Integer eventId);
}
