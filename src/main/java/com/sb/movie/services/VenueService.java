package com.sb.movie.services;

import com.sb.movie.entities.Venue;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.request.VenueUpdateRequest;
import com.sb.movie.response.VenueResponse;

import java.util.List;

public interface VenueService {

    VenueResponse addVenue(VenueRequest venueRequest);

    VenueResponse getVenueById(Integer id);

    List<VenueResponse> getAllVenues();

    List<VenueResponse> getVenuesByCity(String city);

    VenueResponse updateVenue(Integer id, VenueUpdateRequest venueUpdateRequest);

    String deleteVenue(Integer id);
}
