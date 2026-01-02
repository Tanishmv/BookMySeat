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
import com.sb.movie.request.EventUpdateRequest;
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
    @CacheEvict(value = "eventSearch", allEntries = true)
    public Event addEvent(EventRequest eventRequest) {
        log.info("Adding new event: {}", eventRequest.getName());

        // Validate genre usage
        validateGenreForEventType(eventRequest.getEventType(), eventRequest.getGenre());

        Event eventByName = eventRepository.findByName(eventRequest.getName());

        if (eventByName != null && eventByName.getLanguage().equals(eventRequest.getLanguage())) {
            throw new EventAlreadyExist("Event with name '" + eventRequest.getName() +
                "' already exists in " + eventRequest.getLanguage() + " language");
        }

        Event event = EventConvertor.eventDtoToEvent(eventRequest);
        Event saved = eventRepository.save(event);

        log.info("Event '{}' added successfully with ID: {}", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "eventById", key = "#eventId", unless = "#result == null")
    public Event getEventById(Integer eventId) {
        log.debug("Fetching event by ID from database: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventDoesNotExist("Event with ID " + eventId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "eventSearch",
               key = "#name + '_' + #city + '_' + #eventType + '_' + #genre + '_' + #language + '_' + #date",
               unless = "#result == null || #result.isEmpty()")
    public List<Event> searchEvents(String name, String city, EventType eventType, Genre genre, Language language, Date date) {
        log.debug("Searching events with filters - name: {}, city: {}, type: {}, genre: {}, language: {}, date: {}",
                  name, city, eventType, genre, language, date);

        // Convert enums to strings for native query
        String eventTypeStr = eventType != null ? eventType.name() : null;
        String genreStr = genre != null ? genre.name() : null;
        String languageStr = language != null ? language.name() : null;

        return eventRepository.searchEvents(name, city, eventTypeStr, genreStr, languageStr, date);
    }

    @Override
    @Transactional
    @Caching(
            put = @org.springframework.cache.annotation.CachePut(value = "eventById", key = "#eventId"),
            evict = @CacheEvict(value = "eventSearch", allEntries = true)
    )
    public Event updateEvent(Integer eventId, EventUpdateRequest eventUpdateRequest) {
        log.info("Updating event with ID: {}", eventId);

        Event existingEvent = getEventById(eventId);

        // Only update fields that are provided (not null)
        if (eventUpdateRequest.getName() != null) {
            existingEvent.setName(eventUpdateRequest.getName());
        }
        if (eventUpdateRequest.getEventType() != null) {
            // Validate genre usage only if eventType is being updated
            validateGenreForEventType(eventUpdateRequest.getEventType(),
                eventUpdateRequest.getGenre() != null ? eventUpdateRequest.getGenre() : existingEvent.getGenre());
            existingEvent.setEventType(eventUpdateRequest.getEventType());
        }
        if (eventUpdateRequest.getDuration() != null) {
            existingEvent.setDuration(eventUpdateRequest.getDuration());
        }
        if (eventUpdateRequest.getRating() != null) {
            existingEvent.setRating(eventUpdateRequest.getRating());
        }
        if (eventUpdateRequest.getReleaseDate() != null) {
            existingEvent.setReleaseDate(eventUpdateRequest.getReleaseDate());
        }
        if (eventUpdateRequest.getGenre() != null) {
            // Validate genre usage if genre is being updated
            validateGenreForEventType(existingEvent.getEventType(), eventUpdateRequest.getGenre());
            existingEvent.setGenre(eventUpdateRequest.getGenre());
        }
        if (eventUpdateRequest.getLanguage() != null) {
            existingEvent.setLanguage(eventUpdateRequest.getLanguage());
        }
        if (eventUpdateRequest.getArtist() != null) {
            existingEvent.setArtist(eventUpdateRequest.getArtist());
        }
        if (eventUpdateRequest.getDirector() != null) {
            existingEvent.setDirector(eventUpdateRequest.getDirector());
        }
        if (eventUpdateRequest.getPerformers() != null) {
            existingEvent.setPerformers(eventUpdateRequest.getPerformers());
        }
        if (eventUpdateRequest.getDescription() != null) {
            existingEvent.setDescription(eventUpdateRequest.getDescription());
        }
        if (eventUpdateRequest.getPosterUrl() != null) {
            existingEvent.setPosterUrl(eventUpdateRequest.getPosterUrl());
        }

        Event updated = eventRepository.save(existingEvent);

        log.info("Event '{}' updated successfully and cache updated", updated.getName());
        return updated;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "eventById", key = "#eventId"),
            @CacheEvict(value = "eventSearch", allEntries = true)
    })
    public String deleteEvent(Integer eventId) {
        log.info("Deleting event with ID: {}", eventId);

        Event event = getEventById(eventId);
        String eventName = event.getName();
        eventRepository.delete(event);

        log.info("Event '{}' deleted successfully and cache evicted", eventName);
        return "Event '" + eventName + "' has been deleted successfully";
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
