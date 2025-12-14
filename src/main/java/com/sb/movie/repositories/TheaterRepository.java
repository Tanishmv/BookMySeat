package com.sb.movie.repositories;

import com.sb.movie.entities.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Integer> {
    Theater findByAddress(String address);
    List<Theater> findByCity(String city);
}
