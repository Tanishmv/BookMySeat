package com.sb.movie.repositories;

import com.sb.movie.entities.Ticket;
import com.sb.movie.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Integer> {
    List<Ticket> findByUser(User user);
}
