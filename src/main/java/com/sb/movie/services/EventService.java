package com.sb.movie.services;

import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.request.EventRequest;

import java.sql.Date;
import java.util.List;

public interface EventService {
    String addEvent(EventRequest eventRequest);

    Event getEventById(Integer eventId);

    List<Event> searchEvents(String name, String city, EventType eventType, Genre genre, Language language, Date date);

    String updateEvent(Integer eventId, EventRequest eventRequest);

    String deleteEvent(Integer eventId);
}
