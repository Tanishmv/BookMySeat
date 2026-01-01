package com.sb.movie.services;

import com.sb.movie.entities.Theater;
import com.sb.movie.exceptions.TheaterIsExist;
import com.sb.movie.exceptions.TheaterIsNotExist;
import com.sb.movie.request.TheaterRequest;
import com.sb.movie.request.TheaterUpdateRequest;
import com.sb.movie.response.TheaterResponse;

import java.util.List;

public interface TheaterService {

    TheaterResponse addTheater(TheaterRequest theaterRequest) throws TheaterIsExist;

    List<TheaterResponse> getAllTheaters();

    TheaterResponse getTheaterById(Integer id) throws TheaterIsNotExist;

    List<TheaterResponse> getTheatersByCity(String city);

    TheaterResponse updateTheater(Integer id, TheaterUpdateRequest theaterUpdateRequest) throws TheaterIsNotExist;

    String deleteTheater(Integer id) throws TheaterIsNotExist;
}
