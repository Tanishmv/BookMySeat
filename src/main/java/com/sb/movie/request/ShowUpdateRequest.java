package com.sb.movie.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;

@Data
public class ShowUpdateRequest {
    @NotNull(message = "Show date is required")
    private Date showDate;

    @NotNull(message = "Show start time is required")
    private Time showStartTime;
}
