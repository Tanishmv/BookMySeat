package com.sb.movie.controllers;

import com.sb.movie.entities.Show;
import com.sb.movie.request.ShowRequest;
import com.sb.movie.request.ShowSeatRequest;
import com.sb.movie.services.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Tag(name = "Show Management", description = "APIs for managing shows and viewing show schedules")
public class ShowController {

    private final ShowService showService;

    @PostMapping("/addNew")
    public ResponseEntity<String> addShow(@RequestBody ShowRequest showRequest) {
        try {
            String result = showService.addShow(showRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/associateSeats")
    public ResponseEntity<String> associateShowSeats(@RequestBody ShowSeatRequest showSeatRequest) {
        try {
            String result = showService.associateShowSeats(showSeatRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show by ID", description = "Retrieve show details by ID including time, date, venue, and seat information")
    public ResponseEntity<?> getShowById(@PathVariable Integer id) {
        try {
            Show show = showService.getShowById(id);
            return new ResponseEntity<>(show, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @Operation(summary = "Get all shows", description = "Retrieve all shows ordered by date and venue")
    public ResponseEntity<List<Show>> getAllShows() {
        List<Show> shows = showService.getAllShows();
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get shows by event", description = "Retrieve all shows for a specific event")
    public ResponseEntity<List<Show>> getShowsByEventId(@PathVariable Integer eventId) {
        List<Show> shows = showService.getShowsByEventId(eventId);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    @GetMapping("/theater/{theaterId}")
    @Operation(summary = "Get shows by theater", description = "Retrieve all shows for a specific theater/venue")
    public ResponseEntity<List<Show>> getShowsByTheaterId(@PathVariable Integer theaterId) {
        List<Show> shows = showService.getShowsByTheaterId(theaterId);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get shows by date", description = "Retrieve all shows on a specific date (format: yyyy-MM-dd)")
    public ResponseEntity<List<Show>> getShowsByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Show> shows = showService.getShowsByDate(date);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    @GetMapping("/grouped")
    @Operation(summary = "Get shows grouped by date and venue",
               description = "Retrieve all shows grouped first by date, then by venue name")
    public ResponseEntity<Map<String, Map<String, List<Show>>>> getShowsGroupedByDateAndVenue() {
        Map<String, Map<String, List<Show>>> groupedShows = showService.getShowsGroupedByDateAndVenue();
        return new ResponseEntity<>(groupedShows, HttpStatus.OK);
    }

}
