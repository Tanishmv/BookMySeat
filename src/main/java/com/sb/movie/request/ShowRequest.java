package com.sb.movie.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;

@Data
public class ShowRequest {

    @NotNull(message = "Show start time is required")
    private Time showStartTime;

    @NotNull(message = "Show date is required")
    private Date showDate;

    @NotNull(message = "Theater ID is required")
    private Integer theaterId;

    @NotNull(message = "Event ID is required")
    private Integer eventId;

    @NotNull(message = "Price for premium seats is required")
    @Min(value = 1, message = "Price for premium seats must be greater than 0")
    private Integer priceOfPremiumSeat;

    @NotNull(message = "Price for classic seats is required")
    @Min(value = 1, message = "Price for classic seats must be greater than 0")
    private Integer priceOfClassicSeat;
}
