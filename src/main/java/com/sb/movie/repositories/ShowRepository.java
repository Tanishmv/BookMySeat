package com.sb.movie.repositories;

import com.sb.movie.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Integer> {

    List<Show> findByEventId(Integer eventId);

    List<Show> findByTheaterId(Integer theaterId);

    List<Show> findByDate(Date date);

    @Query("SELECT s FROM Show s WHERE s.date = :date AND s.theater.id = :theaterId")
    List<Show> findByDateAndTheaterId(@Param("date") Date date, @Param("theaterId") Integer theaterId);

    @Query("SELECT s FROM Show s ORDER BY s.date, s.theater.name, s.time")
    List<Show> findAllOrderedByDateAndVenue();
}
