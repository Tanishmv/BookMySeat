package com.sb.movie.services;

import com.sb.movie.entities.Theater;
import com.sb.movie.exceptions.TheaterIsExist;
import com.sb.movie.exceptions.TheaterIsNotExist;
import com.sb.movie.request.TheaterRequest;
import com.sb.movie.request.TheaterSeatRequest;

import java.util.List;

public interface TheaterService {

    String addTheater(TheaterRequest theaterRequest) throws TheaterIsExist;

    String addTheaterSeat(TheaterSeatRequest entryDto) throws TheaterIsNotExist;

    List<Theater> getAllTheaters();

    Theater getTheaterById(Integer id) throws TheaterIsNotExist;

    List<Theater> getTheatersByCity(String city);
}
