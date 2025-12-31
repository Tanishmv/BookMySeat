package com.sb.movie.controllers;

import com.sb.movie.request.TheaterRequest;
import com.sb.movie.request.TheaterUpdateRequest;
import com.sb.movie.response.TheaterResponse;
import com.sb.movie.services.TheaterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
@RequestMapping("/theater")
@Tag(name = "Theater Management", description = "APIs for managing theaters and their configurations. Theaters belong to venues and contain seats.")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;

    @PostMapping("/addNew")
    @Operation(
            summary = "Create a new theater",
            description = "Creates a new theater with auto-generated seat layout. Seats are created based on the provided configuration (classic seats, premium seats, and seats per row)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Theater created successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or venue not found")
    })
    public ResponseEntity<String> addTheater(@RequestBody TheaterRequest request) {
        try {
            String result = theaterService.addTheater(request);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @Operation(
            summary = "Get all theaters or filter by city",
            description = "Retrieves all theaters with detailed information or filters by city if the city parameter is provided. Includes venue info, seat statistics, and show count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Theaters retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TheaterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<List<TheaterResponse>> getTheaters(
            @Parameter(description = "City name to filter theaters (optional)")
            @RequestParam(required = false) String city) {
        try {
            List<TheaterResponse> theaters;
            if (city != null && !city.isEmpty()) {
                theaters = theaterService.getTheatersByCity(city);
            } else {
                theaters = theaterService.getAllTheaters();
            }
            return new ResponseEntity<>(theaters, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get theater by ID",
            description = "Retrieves detailed information about a specific theater including venue info, all seats, seat type breakdown, and total shows."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Theater found",
                    content = @Content(schema = @Schema(implementation = TheaterResponse.class))),
            @ApiResponse(responseCode = "404", description = "Theater not found")
    })
    public ResponseEntity<TheaterResponse> getTheaterById(
            @Parameter(description = "Theater ID", required = true)
            @PathVariable Integer id) {
        try {
            TheaterResponse theater = theaterService.getTheaterById(id);
            return new ResponseEntity<>(theater, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update theater name",
            description = "Updates only the theater name. Venue and seat configuration cannot be updated after creation since a theater is physically located at a specific venue. Request body must contain only the 'name' field."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Theater updated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or name is blank"),
            @ApiResponse(responseCode = "404", description = "Theater not found")
    })
    public ResponseEntity<String> updateTheater(
            @Parameter(description = "Theater ID", required = true)
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Theater update request with only name field",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TheaterUpdateRequest.class)))
            @Valid @RequestBody TheaterUpdateRequest request) {
        try {
            String result = theaterService.updateTheater(id, request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete theater",
            description = "Deletes a theater and all its associated data. WARNING: This will cascade delete all shows and tickets associated with this theater."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Theater deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Cannot delete theater"),
            @ApiResponse(responseCode = "404", description = "Theater not found")
    })
    public ResponseEntity<String> deleteTheater(
            @Parameter(description = "Theater ID", required = true)
            @PathVariable Integer id) {
        try {
            String result = theaterService.deleteTheater(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
