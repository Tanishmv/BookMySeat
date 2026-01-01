package com.sb.movie.controllers;

import com.sb.movie.request.VenueRequest;
import com.sb.movie.response.VenueResponse;
import com.sb.movie.services.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venue")
@Tag(name = "Venue Management", description = "APIs for managing venues. Venues are physical locations that contain theaters.")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @PostMapping("/addNew")
    @Operation(
            summary = "Create a new venue",
            description = "Creates a new venue with unique address validation. Each venue represents a physical location where events are held."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venue created successfully",
                    content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or venue already exists at the address")
    })
    public ResponseEntity<?> addVenue(@RequestBody VenueRequest request) {
        try {
            VenueResponse venue = venueService.addVenue(request);
            return new ResponseEntity<>(venue, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @Operation(
            summary = "Get all venues or filter by city",
            description = "Retrieves all venues or filters by city if the city parameter is provided. Results are sorted by name when filtered by city."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venues retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<List<VenueResponse>> getVenues(
            @Parameter(description = "City name to filter venues (optional)")
            @RequestParam(required = false) String city) {
        try {
            List<VenueResponse> venues;
            if (city != null && !city.isEmpty()) {
                venues = venueService.getVenuesByCity(city);
            } else {
                venues = venueService.getAllVenues();
            }
            return new ResponseEntity<>(venues, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get venue by ID",
            description = "Retrieves detailed information about a specific venue including its theaters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue found",
                    content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<VenueResponse> getVenueById(
            @Parameter(description = "Venue ID", required = true)
            @PathVariable Integer id) {
        try {
            VenueResponse venue = venueService.getVenueById(id);
            return new ResponseEntity<>(venue, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update venue details",
            description = "Updates venue information including name, address, city, and description."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue updated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<?> updateVenue(
            @Parameter(description = "Venue ID", required = true)
            @PathVariable Integer id,
            @RequestBody VenueRequest request) {
        try {
            com.sb.movie.response.VenueResponse venue = venueService.updateVenue(id, request);
            return new ResponseEntity<>(venue, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete venue",
            description = "Deletes a venue and all its associated data. WARNING: This will cascade delete all theaters, shows, and tickets associated with this venue."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Cannot delete venue"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<String> deleteVenue(
            @Parameter(description = "Venue ID", required = true)
            @PathVariable Integer id) {
        try {
            String result = venueService.deleteVenue(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
