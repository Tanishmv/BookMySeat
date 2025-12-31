package com.sb.movie.repositories;

import com.sb.movie.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Integer> {

    @Query("SELECT s FROM Show s WHERE " +
           "(:eventId IS NULL OR s.event.id = :eventId) AND " +
           "(:theaterId IS NULL OR s.theater.id = :theaterId) AND " +
           "(CAST(:date AS date) IS NULL OR s.date = :date) " +
           "ORDER BY s.date, s.theater.name, s.time")
    List<Show> searchShows(@Param("eventId") Integer eventId,
                           @Param("theaterId") Integer theaterId,
                           @Param("date") Date date);
}
