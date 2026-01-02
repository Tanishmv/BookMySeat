package com.sb.movie.request;

import lombok.Data;

@Data
public class VenueUpdateRequest {
    private String name;
    private String address;
    private String city;
    private String description;
}
