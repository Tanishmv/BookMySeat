package com.sb.movie.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatLockRequest {
    @NotNull(message = "Show ID is required")
    private Integer showId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> requestSeats;
}
