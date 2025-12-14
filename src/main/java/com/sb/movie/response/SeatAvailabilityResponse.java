package com.sb.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    // Show details
    private Integer showId;
    private Date showDate;
    private Time showTime;
    private String eventName;
    private String theaterName;
    private String theaterAddress;
    private String city;

    // Seat statistics
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer lockedSeats;
    private Integer bookedSeats;

    // Detailed seat list
    private List<SeatInfo> seats;
}
