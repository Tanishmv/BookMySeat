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
public class BookingFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String userEmail;
    private Integer showId;
    private String eventName;
    private String requestedSeats;
    private String failureReason;
    private LocalDateTime failureTime;
}
