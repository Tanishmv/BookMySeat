package com.sb.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistoryResponse {
    private Integer ticketId;
    private Date bookedAt;
    private Integer totalPrice;
    private String bookedSeats;

    // Show details
    private Integer showId;
    private Date showDate;
    private Time showTime;

    // Event details
    private String eventName;
    private String eventType;

    // Theater details
    private String theaterName;
    private String theaterAddress;
    private String city;
}
