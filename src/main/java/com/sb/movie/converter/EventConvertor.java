package com.sb.movie.converter;

import com.sb.movie.entities.Event;
import com.sb.movie.request.EventRequest;

public class EventConvertor {

    public static Event eventDtoToEvent(EventRequest eventRequest) {
        Event event = Event.builder()
                .name(eventRequest.getName())
                .eventType(eventRequest.getEventType())
                .duration(eventRequest.getDuration())
                .genre(eventRequest.getGenre())
                .language(eventRequest.getLanguage())
                .releaseDate(eventRequest.getReleaseDate())
                .rating(eventRequest.getRating())
                .artist(eventRequest.getArtist())
                .director(eventRequest.getDirector())
                .performers(eventRequest.getPerformers())
                .description(eventRequest.getDescription())
                .posterUrl(eventRequest.getPosterUrl())
                .city(eventRequest.getCity())
                .build();

        return event;
    }
}
