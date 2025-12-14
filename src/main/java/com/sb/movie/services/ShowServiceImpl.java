package com.sb.movie.services;

import com.sb.movie.converter.ShowConvertor;
import com.sb.movie.entities.*;
import com.sb.movie.enums.SeatType;
import com.sb.movie.exceptions.EventDoesNotExist;
import com.sb.movie.exceptions.ShowDoesNotExists;
import com.sb.movie.exceptions.TheaterDoesNotExists;
import com.sb.movie.repositories.EventRepository;
import com.sb.movie.repositories.ShowRepository;
import com.sb.movie.repositories.TheaterRepository;
import com.sb.movie.request.ShowRequest;
import com.sb.movie.request.ShowSeatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowServiceImpl implements ShowService{

    private final EventRepository eventRepository;
    private final TheaterRepository theaterRepository;
    private final ShowRepository showRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "shows", allEntries = true),
            @CacheEvict(value = "eventById", key = "#showRequest.eventId")
    })
    public String addShow(ShowRequest showRequest) {
        log.info("Adding new show for event ID: {} at theater ID: {}",
                showRequest.getEventId(), showRequest.getTheaterId());

        Show show = ShowConvertor.showDtoToShow(showRequest);

        Optional<Event> eventOpt = eventRepository.findById(showRequest.getEventId());

        if (eventOpt.isEmpty()) {
            throw new EventDoesNotExist();
        }

        Optional<Theater> theaterOpt = theaterRepository.findById(showRequest.getTheaterId());

        if (theaterOpt.isEmpty()) {
            throw new TheaterDoesNotExists();
        }

        Theater theater = theaterOpt.get();
        Event event = eventOpt.get();

        show.setEvent(event);
        show.setTheater(theater);
        show = showRepository.save(show);

        event.getShows().add(show);
        theater.getShowList().add(show);

        eventRepository.save(event);
        theaterRepository.save(theater);

        log.info("Show added successfully with ID: {} and cache evicted", show.getShowId());
        return "Show has been added Successfully";
    }

    @Override
    @Transactional
    @CacheEvict(value = "showById", key = "#showSeatRequest.showId")
    public String associateShowSeats(ShowSeatRequest showSeatRequest) throws ShowDoesNotExists {
        log.info("Associating seats for show ID: {}", showSeatRequest.getShowId());

        Optional<Show> showOpt = showRepository.findById(showSeatRequest.getShowId());

        if (showOpt.isEmpty()) {
            throw new ShowDoesNotExists();
        }

        Show show = showOpt.get();
        Theater theater = show.getTheater();

        List<TheaterSeat> theaterSeatList = theater.getTheaterSeatList();

        List<ShowSeat> showSeatList = show.getShowSeatList();

        for (TheaterSeat theaterSeat : theaterSeatList) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setSeatNo(theaterSeat.getSeatNo());
            showSeat.setSeatType(theaterSeat.getSeatType());

            if (showSeat.getSeatType().equals(SeatType.CLASSIC)) {
                showSeat.setPrice((showSeatRequest.getPriceOfClassicSeat()));
            } else {
                showSeat.setPrice(showSeatRequest.getPriceOfPremiumSeat());
            }

            showSeat.setShow(show);
            showSeat.setStatus(com.sb.movie.enums.SeatStatus.AVAILABLE);
            showSeat.setIsFoodContains(Boolean.FALSE);

            showSeatList.add(showSeat);
        }

        showRepository.save(show);

        log.info("Successfully associated {} seats for show ID: {} and cache evicted",
                showSeatList.size(), showSeatRequest.getShowId());
        return "Show seats have been associated successfully";
    }

    @Override
    @Cacheable(value = "showById", key = "#showId", unless = "#result == null")
    public Show getShowById(Integer showId) {
        log.debug("Fetching show by ID from database: {}", showId);
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowDoesNotExists());
    }

    @Override
    @Cacheable(value = "shows", unless = "#result == null || #result.isEmpty()")
    public List<Show> getAllShows() {
        log.debug("Fetching all shows from database");
        return showRepository.findAllOrderedByDateAndVenue();
    }

    @Override
    @Cacheable(value = "showsByEvent", key = "#eventId", unless = "#result == null || #result.isEmpty()")
    public List<Show> getShowsByEventId(Integer eventId) {
        log.debug("Fetching shows by event ID from database: {}", eventId);
        return showRepository.findByEventId(eventId);
    }

    @Override
    @Cacheable(value = "showsByTheater", key = "#theaterId", unless = "#result == null || #result.isEmpty()")
    public List<Show> getShowsByTheaterId(Integer theaterId) {
        log.debug("Fetching shows by theater ID from database: {}", theaterId);
        return showRepository.findByTheaterId(theaterId);
    }

    @Override
    @Cacheable(value = "showsByDate", key = "#date", unless = "#result == null || #result.isEmpty()")
    public List<Show> getShowsByDate(Date date) {
        log.debug("Fetching shows by date from database: {}", date);
        return showRepository.findByDate(date);
    }

    @Override
    @Cacheable(value = "showsGrouped", unless = "#result == null || #result.isEmpty()")
    public Map<String, Map<String, List<Show>>> getShowsGroupedByDateAndVenue() {
        log.debug("Fetching shows grouped by date and venue from database");
        List<Show> shows = showRepository.findAllOrderedByDateAndVenue();

        return shows.stream()
                .collect(Collectors.groupingBy(
                        show -> show.getDate().toString(),
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                show -> show.getTheater().getName(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ));
    }
}