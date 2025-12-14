package com.sb.movie.repositories;

import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Event findByName(String name);

    List<Event> findByEventType(EventType eventType);

    List<Event> findByCity(String city);

    @Query("SELECT e FROM Event e WHERE e.city = :city AND e.eventType = :eventType")
    List<Event> findByCityAndEventType(@Param("city") String city, @Param("eventType") EventType eventType);

    List<Event> findByGenre(Genre genre);

    List<Event> findByLanguage(Language language);

    List<Event> findByReleaseDate(Date releaseDate);

    @Query("SELECT DISTINCT e FROM Event e JOIN e.shows s WHERE s.date = :date")
    List<Event> findEventsWithShowsOnDate(@Param("date") Date date);
}
