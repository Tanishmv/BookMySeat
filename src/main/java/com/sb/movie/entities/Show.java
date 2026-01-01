package com.sb.movie.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "SHOWS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer showId;

    private Time time;

    private Date date;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Event event;

    @ManyToOne
    @JoinColumn
    private Theater theater;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<ShowSeat> showSeatList = new ArrayList<>();

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Ticket> ticketList = new ArrayList<>();

    // Helper methods for better API responses

    @Transient
    public Time getStartTime() {
        return time;
    }

    @Transient
    public Time getEndTime() {
        if (time == null || event == null || event.getDuration() == null) {
            return null;
        }
        // Calculate end time = start time + event duration (in minutes)
        long startMillis = time.getTime();
        long durationMillis = event.getDuration() * 60 * 1000L;
        return new Time(startMillis + durationMillis);
    }

    @Transient
    public Date getShowDate() {
        return date;
    }

    @Transient
    public Time getShowTime() {
        return time;
    }
}
