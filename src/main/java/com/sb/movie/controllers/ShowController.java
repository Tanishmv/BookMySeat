package com.sb.movie.controllers;

import com.sb.movie.entities.Show;
import com.sb.movie.request.ShowRequest;
import com.sb.movie.request.ShowUpdateRequest;
import com.sb.movie.response.SeatAvailabilityResponse;
import com.sb.movie.response.ShowDetailsResponse;
import com.sb.movie.services.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Tag(name = "Show Management", description = "APIs for managing shows and viewing show schedules")
public class ShowController {

    private final ShowService showService;

    @PostMapping("/addNew")
    @Operation(summary = "Create new show with seats",
               description = "Create a new show for an event at a specific venue with seat prices (Admin only). " +
                           "Seats are automatically created based on theater seats.")
    public ResponseEntity<?> addShow(@Valid @RequestBody ShowRequest showRequest) {
        try {
            Show show = showService.addShow(showRequest);
            return new ResponseEntity<>(show, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show details",
               description = "Retrieve comprehensive show details including start/end time, venue location, " +
                           "seat categories with availability summary")
    public ResponseEntity<?> getShowById(@PathVariable Integer id) {
        try {
            ShowDetailsResponse showDetails = showService.getShowDetails(id);
            return new ResponseEntity<>(showDetails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @Operation(summary = "Search shows",
               description = "Search shows with optional filters: eventId, theaterId, date (format: yyyy-MM-dd). " +
                           "All parameters are optional. Leave blank to get all shows.")
    public ResponseEntity<List<Show>> searchShows(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) Integer theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Convert LocalDate to java.sql.Date for service layer
        Date sqlDate = date != null ? Date.valueOf(date) : null;
        List<Show> shows = showService.searchShows(eventId, theaterId, sqlDate);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    @GetMapping("/grouped")
    @Operation(summary = "Get shows grouped by date and venue",
               description = "Retrieve all shows grouped first by date, then by venue name. " +
                           "Useful for displaying schedules and calendars.")
    public ResponseEntity<Map<String, Map<String, List<Show>>>> getShowsGroupedByDateAndVenue() {
        Map<String, Map<String, List<Show>>> groupedShows = showService.getShowsGroupedByDateAndVenue();
        return new ResponseEntity<>(groupedShows, HttpStatus.OK);
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "Get real-time seat availability",
               description = "View seat availability with counts and detailed seat status for a show")
    public ResponseEntity<?> getSeatAvailability(@PathVariable Integer id) {
        try {
            SeatAvailabilityResponse availability = showService.getSeatAvailability(id);
            return new ResponseEntity<>(availability, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update show", description = "Update show date and time")
    public ResponseEntity<?> updateShow(@PathVariable Integer id, @Valid @RequestBody ShowUpdateRequest showUpdateRequest) {
        try {
            Show show = showService.updateShow(id, showUpdateRequest);
            return new ResponseEntity<>(show, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete show", description = "Delete a show (only if no bookings exist)")
    public ResponseEntity<String> deleteShow(@PathVariable Integer id) {
        try {
            String result = showService.deleteShow(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
