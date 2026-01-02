package com.sb.movie.controllers;

import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.request.EventRequest;
import com.sb.movie.request.EventUpdateRequest;
import com.sb.movie.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
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
    public ResponseEntity<?> addEvent(@Valid @RequestBody EventRequest eventRequest) {
        try {
            Event event = eventService.addEvent(eventRequest);
            return new ResponseEntity<>(event, HttpStatus.CREATED);
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
    @Operation(summary = "Search events",
               description = "Search events with optional filters: name, city, type, genre, language, showDate, releaseDate (format: yyyy-MM-dd). " +
                           "All parameters are optional. Leave blank to get all events. " +
                           "Name search is partial match (case-insensitive). " +
                           "showDate filters events that have shows scheduled on that date. " +
                           "releaseDate filters events released on that date.")
    public ResponseEntity<List<Event>> searchEvents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) Language language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate) {
        // Convert LocalDate to java.sql.Date for service layer
        Date sqlShowDate = showDate != null ? Date.valueOf(showDate) : null;
        Date sqlReleaseDate = releaseDate != null ? Date.valueOf(releaseDate) : null;
        List<Event> events = eventService.searchEvents(name, city, type, genre, language, sqlShowDate, sqlReleaseDate);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update event", description = "Update an existing event (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> updateEvent(
            @PathVariable Integer id,
            @Valid @RequestBody EventUpdateRequest eventUpdateRequest) {
        try {
            Event event = eventService.updateEvent(id, eventUpdateRequest);
            return new ResponseEntity<>(event, HttpStatus.OK);
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
