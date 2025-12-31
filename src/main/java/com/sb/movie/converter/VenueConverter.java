package com.sb.movie.converter;

import com.sb.movie.entities.Theater;
import com.sb.movie.entities.Venue;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.response.VenueResponse;

import java.util.stream.Collectors;

public class VenueConverter {

    public static Venue venueRequestToVenue(VenueRequest venueRequest) {
        return Venue.builder()
                .name(venueRequest.getName())
                .address(venueRequest.getAddress())
                .city(venueRequest.getCity())
                .description(venueRequest.getDescription())
                .build();
    }

    public static VenueResponse venueToVenueResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .description(venue.getDescription())
                .totalTheaters(venue.getTheaters() != null ? venue.getTheaters().size() : 0)
                .theaters(venue.getTheaters() != null ?
                    venue.getTheaters().stream()
                        .map(theater -> VenueResponse.TheaterInfo.builder()
                            .id(theater.getId())
                            .name(theater.getName())
                            .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
