package com.sb.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {
    private List<String> lockedSeats;
    private Integer totalPrice;
    private LocalDateTime lockedAt;
    private LocalDateTime expiresAt;
    private String message;
}
