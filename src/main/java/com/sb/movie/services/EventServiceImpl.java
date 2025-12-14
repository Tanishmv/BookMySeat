package com.sb.movie.services;

import com.sb.movie.converter.EventConvertor;
import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.exceptions.EventAlreadyExist;
import com.sb.movie.exceptions.EventDoesNotExist;
import com.sb.movie.repositories.EventRepository;
import com.sb.movie.request.EventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventsByType", allEntries = true),
            @CacheEvict(value = "eventsByCity", allEntries = true),
            @CacheEvict(value = "eventsByGenre", allEntries = true),
            @CacheEvict(value = "eventsByLanguage", allEntries = true),
            @CacheEvict(value = "eventsByDate", allEntries = true)
    })
    public String addEvent(EventRequest eventRequest) {
        log.info("Adding new event: {}", eventRequest.getName());

        // Validate genre usage
        validateGenreForEventType(eventRequest.getEventType(), eventRequest.getGenre());

        Event eventByName = eventRepository.findByName(eventRequest.getName());

        if (eventByName != null && eventByName.getLanguage().equals(eventRequest.getLanguage())) {
            throw new EventAlreadyExist("Event with name '" + eventRequest.getName() +
                "' already exists in " + eventRequest.getLanguage() + " language");
        }

        Event event = EventConvertor.eventDtoToEvent(eventRequest);
        eventRepository.save(event);

        log.info("Event '{}' added successfully and cache evicted", event.getName());
        return "Event '" + event.getName() + "' has been added successfully";
    }

    @Override
    @Cacheable(value = "eventById", key = "#eventId", unless = "#result == null")
    public Event getEventById(Integer eventId) {
        log.debug("Fetching event by ID from database: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventDoesNotExist("Event with ID " + eventId + " not found"));
    }

    @Override
    @Cacheable(value = "events", unless = "#result == null || #result.isEmpty()")
    public List<Event> getAllEvents() {
        log.debug("Fetching all events from database");
        return eventRepository.findAll();
    }

    @Override
    @Cacheable(value = "eventsByType", key = "#eventType", unless = "#result == null || #result.isEmpty()")
    public List<Event> getEventsByType(EventType eventType) {
        log.debug("Fetching events by type from database: {}", eventType);
        return eventRepository.findByEventType(eventType);
    }

    @Override
    @Cacheable(value = "eventsByCity", key = "#city", unless = "#result == null || #result.isEmpty()")
    public List<Event> getEventsByCity(String city) {
        log.debug("Fetching events by city from database: {}", city);
        return eventRepository.findByCity(city);
    }

    @Override
    @Cacheable(value = "eventsByCity", key = "#city + '_' + #eventType", unless = "#result == null || #result.isEmpty()")
    public List<Event> getEventsByCityAndType(String city, EventType eventType) {
        log.debug("Fetching events by city and type from database: {} - {}", city, eventType);
        return eventRepository.findByCityAndEventType(city, eventType);
    }

    @Override
    @Cacheable(value = "eventsByGenre", key = "#genre", unless = "#result == null || #result.isEmpty()")
    public List<Event> getEventsByGenre(Genre genre) {
        log.debug("Fetching events by genre from database: {}", genre);
        return eventRepository.findByGenre(genre);
    }

    @Override
    @Cacheable(value = "eventsByLanguage", key = "#language", unless = "#result == null || #result.isEmpty()")
    public List<Event> getEventsByLanguage(Language language) {
        log.debug("Fetching events by language from database: {}", language);
        return eventRepository.findByLanguage(language);
    }

    @Override
    @Cacheable(value = "eventsByDate", key = "#date", unless = "#result == null || #result.isEmpty()")
    public List<Event> getEventsByDate(Date date) {
        log.debug("Fetching events with shows on date from database: {}", date);
        return eventRepository.findEventsWithShowsOnDate(date);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventById", key = "#eventId"),
            @CacheEvict(value = "eventsByType", allEntries = true),
            @CacheEvict(value = "eventsByCity", allEntries = true),
            @CacheEvict(value = "eventsByGenre", allEntries = true),
            @CacheEvict(value = "eventsByLanguage", allEntries = true),
            @CacheEvict(value = "eventsByDate", allEntries = true)
    })
    public String updateEvent(Integer eventId, EventRequest eventRequest) {
        log.info("Updating event with ID: {}", eventId);

        // Validate genre usage
        validateGenreForEventType(eventRequest.getEventType(), eventRequest.getGenre());

        Event existingEvent = getEventById(eventId);

        existingEvent.setName(eventRequest.getName());
        existingEvent.setEventType(eventRequest.getEventType());
        existingEvent.setDuration(eventRequest.getDuration());
        existingEvent.setRating(eventRequest.getRating());
        existingEvent.setReleaseDate(eventRequest.getReleaseDate());
        existingEvent.setGenre(eventRequest.getGenre());
        existingEvent.setLanguage(eventRequest.getLanguage());
        existingEvent.setArtist(eventRequest.getArtist());
        existingEvent.setDirector(eventRequest.getDirector());
        existingEvent.setPerformers(eventRequest.getPerformers());
        existingEvent.setDescription(eventRequest.getDescription());
        existingEvent.setPosterUrl(eventRequest.getPosterUrl());
        existingEvent.setCity(eventRequest.getCity());

        eventRepository.save(existingEvent);

        log.info("Event '{}' updated successfully and cache evicted", existingEvent.getName());
        return "Event '" + existingEvent.getName() + "' has been updated successfully";
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventById", key = "#eventId"),
            @CacheEvict(value = "eventsByType", allEntries = true),
            @CacheEvict(value = "eventsByCity", allEntries = true),
            @CacheEvict(value = "eventsByGenre", allEntries = true),
            @CacheEvict(value = "eventsByLanguage", allEntries = true),
            @CacheEvict(value = "eventsByDate", allEntries = true)
    })
    public String deleteEvent(Integer eventId) {
        log.info("Deleting event with ID: {}", eventId);

        Event event = getEventById(eventId);
        eventRepository.delete(event);

        log.info("Event '{}' deleted successfully and cache evicted", event.getName());
        return "Event '" + event.getName() + "' has been deleted successfully";
    }

    /**
     * Validates that genre is only set for event types that support it
     * Genre is only applicable for MOVIE, THEATER, and OPERA events
     */
    private void validateGenreForEventType(EventType eventType, Genre genre) {
        if (genre != null) {
            // Genre is only valid for MOVIE, THEATER, and OPERA
            if (eventType != EventType.MOVIE &&
                eventType != EventType.THEATER &&
                eventType != EventType.OPERA) {
                throw new IllegalArgumentException(
                    "Genre is not applicable for " + eventType + " events. " +
                    "Genre can only be set for MOVIE, THEATER, or OPERA events.");
            }
        }
    }
}
