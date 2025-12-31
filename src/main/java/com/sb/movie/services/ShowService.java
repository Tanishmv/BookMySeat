package com.sb.movie.services;

import com.sb.movie.entities.Show;
import com.sb.movie.exceptions.ShowDoesNotExists;
import com.sb.movie.request.ShowRequest;
import com.sb.movie.response.SeatAvailabilityResponse;
import com.sb.movie.response.ShowDetailsResponse;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface ShowService {

    String addShow(ShowRequest showRequest);

    Show getShowById(Integer showId);

    ShowDetailsResponse getShowDetails(Integer showId) throws ShowDoesNotExists;

    List<Show> searchShows(Integer eventId, Integer theaterId, Date date);

    Map<String, Map<String, List<Show>>> getShowsGroupedByDateAndVenue();

    String updateShow(Integer showId, ShowRequest showRequest) throws ShowDoesNotExists;

    String deleteShow(Integer showId) throws ShowDoesNotExists;

    SeatAvailabilityResponse getSeatAvailability(Integer showId) throws ShowDoesNotExists;
}
