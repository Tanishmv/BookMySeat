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

    @Query(value = "SELECT DISTINCT e.* FROM events e " +
           "LEFT JOIN shows s ON e.id = s.event_id " +
           "LEFT JOIN theaters t ON s.theater_id = t.id " +
           "LEFT JOIN venues v ON t.venue_id = v.id WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:name AS VARCHAR), '%'))) AND " +
           "(:city IS NULL OR LOWER(v.city) = LOWER(CAST(:city AS VARCHAR))) AND " +
           "(:eventType IS NULL OR e.event_type = CAST(:eventType AS VARCHAR)) AND " +
           "(:genre IS NULL OR e.genre = CAST(:genre AS VARCHAR)) AND " +
           "(:language IS NULL OR e.language = CAST(:language AS VARCHAR)) AND " +
           "(CAST(:showDate AS DATE) IS NULL AND CAST(:releaseDate AS DATE) IS NULL OR " +
           "CAST(:showDate AS DATE) IS NOT NULL AND s.date = :showDate OR " +
           "CAST(:releaseDate AS DATE) IS NOT NULL AND e.release_date = :releaseDate)",
           nativeQuery = true)
    List<Event> searchEvents(@Param("name") String name,
                             @Param("city") String city,
                             @Param("eventType") String eventType,
                             @Param("genre") String genre,
                             @Param("language") String language,
                             @Param("showDate") Date showDate,
                             @Param("releaseDate") Date releaseDate);
}
