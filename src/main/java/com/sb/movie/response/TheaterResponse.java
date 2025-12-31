package com.sb.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterResponse {

    private Integer id;
    private String name;
    private VenueInfo venue;
    private Integer totalSeats;
    private Integer totalClassicSeats;
    private Integer totalPremiumSeats;
    private Integer totalShows;
    private List<SeatInfo> seats;

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
    public static class SeatInfo {
        private Integer id;
        private String seatNo;
        private String seatType;
    }
}
