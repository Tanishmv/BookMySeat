package com.sb.movie.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TheaterUpdateRequest {

    @NotBlank(message = "Theater name is required")
    private String name;
}
