package com.sb.movie.request;

import lombok.Data;

@Data
public class TheaterSeatRequest {
    private Integer theaterId;
    private Integer noOfSeatInRow;
    private Integer noOfPremiumSeat;
    private Integer noOfClassicSeat;
}
