package com.sb.movie.controllers;

import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.request.EventRequest;
import com.sb.movie.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for managing events (movies, concerts, sports, etc.)")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new event", description = "Create a new event (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> addEvent(@RequestBody EventRequest eventRequest) {
        try {
            String result = eventService.addEvent(eventRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieve event details by ID")
    public ResponseEntity<?> getEventById(@PathVariable Integer id) {
        try {
            Event event = eventService.getEventById(id);
            return new ResponseEntity<>(event, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieve all events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/type/{eventType}")
    @Operation(summary = "Get events by type", description = "Retrieve events by type (MOVIE, CONCERT, SPORTS, etc.)")
    public ResponseEntity<List<Event>> getEventsByType(@PathVariable EventType eventType) {
        List<Event> events = eventService.getEventsByType(eventType);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get events by city", description = "Retrieve events happening in a specific city")
    public ResponseEntity<List<Event>> getEventsByCity(@PathVariable String city) {
        List<Event> events = eventService.getEventsByCity(city);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search events", description = "Search events by city and type")
    public ResponseEntity<List<Event>> searchEvents(
            @RequestParam String city,
            @RequestParam EventType eventType) {
        List<Event> events = eventService.getEventsByCityAndType(city, eventType);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/genre/{genre}")
    @Operation(summary = "Get events by genre", description = "Retrieve events by genre (DRAMA, ACTION, COMEDY, etc.)")
    public ResponseEntity<List<Event>> getEventsByGenre(@PathVariable Genre genre) {
        List<Event> events = eventService.getEventsByGenre(genre);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/language/{language}")
    @Operation(summary = "Get events by language", description = "Retrieve events by language (HINDI, ENGLISH, TAMIL, etc.)")
    public ResponseEntity<List<Event>> getEventsByLanguage(@PathVariable Language language) {
        List<Event> events = eventService.getEventsByLanguage(language);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get events by date", description = "Retrieve events that have shows on a specific date (format: yyyy-MM-dd)")
    public ResponseEntity<List<Event>> getEventsByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Event> events = eventService.getEventsByDate(date);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update event", description = "Update an existing event (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> updateEvent(
            @PathVariable Integer id,
            @RequestBody EventRequest eventRequest) {
        try {
            String result = eventService.updateEvent(id, eventRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete event", description = "Delete an event (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteEvent(@PathVariable Integer id) {
        try {
            String result = eventService.deleteEvent(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
