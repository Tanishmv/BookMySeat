package com.sb.movie.converter;

import com.sb.movie.entities.Theater;
import com.sb.movie.entities.TheaterSeat;
import com.sb.movie.enums.SeatType;
import com.sb.movie.request.TheaterRequest;
import com.sb.movie.response.TheaterResponse;

import java.util.stream.Collectors;

public class TheaterConvertor {

    public static Theater theaterDtoToTheater(TheaterRequest theaterRequest) {
        Theater theater = Theater.builder()
                .name(theaterRequest.getName())
                .build();
        return theater;
    }

    public static TheaterResponse theaterToTheaterResponse(Theater theater) {
        long classicSeats = 0;
        long premiumSeats = 0;

        if (theater.getTheaterSeatList() != null) {
            classicSeats = theater.getTheaterSeatList().stream()
                    .filter(seat -> seat.getSeatType() == SeatType.CLASSIC)
                    .count();
            premiumSeats = theater.getTheaterSeatList().stream()
                    .filter(seat -> seat.getSeatType() == SeatType.PREMIUM)
                    .count();
        }

        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .venue(theater.getVenue() != null ?
                        TheaterResponse.VenueInfo.builder()
                                .id(theater.getVenue().getId())
                                .name(theater.getVenue().getName())
                                .address(theater.getVenue().getAddress())
                                .city(theater.getVenue().getCity())
                                .build() : null)
                .totalSeats(theater.getTheaterSeatList() != null ? theater.getTheaterSeatList().size() : 0)
                .totalClassicSeats((int) classicSeats)
                .totalPremiumSeats((int) premiumSeats)
                .totalShows(theater.getShowList() != null ? theater.getShowList().size() : 0)
                .seats(theater.getTheaterSeatList() != null ?
                        theater.getTheaterSeatList().stream()
                                .map(seat -> TheaterResponse.SeatInfo.builder()
                                        .id(seat.getId())
                                        .seatNo(seat.getSeatNo())
                                        .seatType(seat.getSeatType().name())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
