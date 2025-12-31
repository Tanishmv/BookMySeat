package com.sb.movie.services;

import com.sb.movie.entities.Venue;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.response.VenueResponse;

import java.util.List;

public interface VenueService {

    String addVenue(VenueRequest venueRequest);

    VenueResponse getVenueById(Integer id);

    List<VenueResponse> getAllVenues();

    List<VenueResponse> getVenuesByCity(String city);

    String updateVenue(Integer id, VenueRequest venueRequest);

    String deleteVenue(Integer id);
}
