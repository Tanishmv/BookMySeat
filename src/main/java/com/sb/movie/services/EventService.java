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

    List<Event> getAllEvents();

    List<Event> getEventsByType(EventType eventType);

    List<Event> getEventsByCity(String city);

    List<Event> getEventsByCityAndType(String city, EventType eventType);

    List<Event> getEventsByGenre(Genre genre);

    List<Event> getEventsByLanguage(Language language);

    List<Event> getEventsByDate(Date date);

    String updateEvent(Integer eventId, EventRequest eventRequest);

    String deleteEvent(Integer eventId);
}
