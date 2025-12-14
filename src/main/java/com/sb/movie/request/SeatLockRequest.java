package com.sb.movie.request;

import lombok.Data;

import java.util.List;

@Data
public class SeatLockRequest {
    private Integer showId;
    private Integer userId;
    private List<String> requestSeats;
}
