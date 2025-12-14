package com.sb.movie.services;

import com.sb.movie.entities.Show;
import com.sb.movie.exceptions.ShowDoesNotExists;
import com.sb.movie.request.ShowRequest;
import com.sb.movie.request.ShowSeatRequest;
import com.sb.movie.response.SeatAvailabilityResponse;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface ShowService {

    String addShow(ShowRequest showRequest);

    String associateShowSeats(ShowSeatRequest showSeatRequest) throws ShowDoesNotExists;

    Show getShowById(Integer showId);

    List<Show> getAllShows();

    List<Show> getShowsByEventId(Integer eventId);

    List<Show> getShowsByTheaterId(Integer theaterId);

    List<Show> getShowsByDate(Date date);

    Map<String, Map<String, List<Show>>> getShowsGroupedByDateAndVenue();

    String updateShow(Integer showId, ShowRequest showRequest) throws ShowDoesNotExists;

    String deleteShow(Integer showId) throws ShowDoesNotExists;

    SeatAvailabilityResponse getSeatAvailability(Integer showId) throws ShowDoesNotExists;
}
