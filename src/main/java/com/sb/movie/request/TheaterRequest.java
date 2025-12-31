package com.sb.movie.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TheaterRequest {

    private String name;

    @NotNull(message = "Venue ID is required")
    private Integer venueId;

    // Seat configuration
    @NotNull(message = "Number of seats in row is required")
    private Integer noOfSeatInRow;

    @NotNull(message = "Number of classic seats is required")
    private Integer noOfClassicSeat;

    @NotNull(message = "Number of premium seats is required")
    private Integer noOfPremiumSeat;
}
