package com.sb.movie.repositories;

import com.sb.movie.entities.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Integer> {

    Venue findByAddress(String address);

    Venue findByName(String name);

    @Query("SELECT v FROM Venue v WHERE LOWER(v.city) = LOWER(:city) ORDER BY v.name ASC")
    List<Venue> findByCityOrderByNameAsc(@Param("city") String city);

    @Query("SELECT v FROM Venue v WHERE " +
           "(:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(v.city) = LOWER(:city))")
    List<Venue> searchVenues(@Param("name") String name,
                             @Param("city") String city);
}
