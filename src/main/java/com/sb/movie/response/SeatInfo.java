package com.sb.movie.response;

import com.sb.movie.enums.SeatStatus;
import com.sb.movie.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatInfo {
    private String seatNo;
    private SeatType seatType;
    private Integer price;
    private SeatStatus status;
}
