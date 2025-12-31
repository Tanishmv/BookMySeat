package com.sb.movie.response;

import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowDetailsResponse {

    // Show basic info
    private Integer showId;
    private Date showDate;
    private Time startTime;
    private Time endTime;

    // Event details
    private Integer eventId;
    private String eventName;
    private EventType eventType;
    private Integer duration;
    private Double rating;
    private Genre genre;
    private Language language;
    private String description;
    private String posterUrl;

    // Venue details
    private VenueInfo venue;

    // Theater details
    private TheaterInfo theater;

    // Seat availability summary (lightweight)
    private Map<String, SeatCategorySummary> seatSummary;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer lockedSeats;
    private Integer bookedSeats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VenueInfo {
        private Integer id;
        private String name;
        private String address;
        private String city;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TheaterInfo {
        private Integer id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatCategorySummary {
        private String seatType;
        private Integer total;
        private Integer available;
        private Integer price;
    }
}
