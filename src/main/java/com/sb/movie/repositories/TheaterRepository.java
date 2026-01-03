package com.sb.movie.repositories;

import com.sb.movie.entities.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Integer> {

    List<Theater> findByVenueId(Integer venueId);

    @Query("SELECT t FROM Theater t WHERE LOWER(t.venue.city) = LOWER(:city)")
    List<Theater> findByCity(@Param("city") String city);

    @Query("SELECT t FROM Theater t WHERE " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(t.venue.city) = LOWER(:city)) AND " +
           "(:venueId IS NULL OR t.venue.id = :venueId)")
    List<Theater> searchTheaters(@Param("name") String name,
                                 @Param("city") String city,
                                 @Param("venueId") Integer venueId);

    @Query("SELECT COUNT(t) > 0 FROM Theater t WHERE " +
           "LOWER(t.name) = LOWER(:name) AND " +
           "t.venue.id = :venueId")
    boolean existsByNameAndVenueId(@Param("name") String name,
                                   @Param("venueId") Integer venueId);
}
