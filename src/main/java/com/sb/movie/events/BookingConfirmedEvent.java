package com.sb.movie.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer bookingId;
    private Integer userId;
    private String userEmail;
    private String userName;
    private String userMobile;

    private Integer showId;
    private String eventName;
    private String eventType;
    private String theaterName;
    private String theaterAddress;
    private LocalDateTime showTime;

    private String bookedSeats;
    private Integer totalSeats;
    private Integer totalPrice;

    private LocalDateTime bookingTime;
    private String bookingReference;
}
