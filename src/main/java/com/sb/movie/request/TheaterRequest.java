package com.sb.movie.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TheaterRequest {

    @NotBlank(message = "Theater name is required")
    private String name;

    @NotNull(message = "Venue ID is required")
    private Integer venueId;

    // Seat configuration
    @NotNull(message = "Number of seats in row is required")
    @Min(value = 1, message = "Number of seats in row must be at least 1")
    private Integer noOfSeatInRow;

    @NotNull(message = "Number of classic seats is required")
    @Min(value = 0, message = "Number of classic seats cannot be negative")
    private Integer noOfClassicSeat;

    @NotNull(message = "Number of premium seats is required")
    @Min(value = 0, message = "Number of premium seats cannot be negative")
    private Integer noOfPremiumSeat;
}
