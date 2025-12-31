package com.sb.movie.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "EVENTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    private Integer duration; // Duration in minutes

    @Column(scale = 2)
    private Double rating;

    private Date releaseDate; // For movies, or event start date

    @Enumerated(value = EnumType.STRING)
    private Genre genre;

    @Enumerated(value = EnumType.STRING)
    private Language language;

    // Event-type specific fields
    private String artist;      // For concerts, comedy shows, dance shows
    private String director;    // For movies, theater
    private String performers;  // For movies, theater, opera (comma-separated list of actors/performers)
    private String description; // General description

    @Column(name = "poster_url")
    private String posterUrl;   // Event poster/image URL

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    private List<Show> shows = new ArrayList<>();
}
