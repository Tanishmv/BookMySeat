package com.sb.movie.services;

import com.sb.movie.converter.ShowConvertor;
import com.sb.movie.entities.*;
import com.sb.movie.enums.SeatStatus;
import com.sb.movie.enums.SeatType;
import com.sb.movie.exceptions.EventDoesNotExist;
import com.sb.movie.exceptions.ShowAlreadyExistsException;
import com.sb.movie.exceptions.ShowDoesNotExists;
import com.sb.movie.exceptions.TheaterDoesNotExists;
import com.sb.movie.repositories.EventRepository;
import com.sb.movie.repositories.ShowRepository;
import com.sb.movie.repositories.TheaterRepository;
import com.sb.movie.request.ShowRequest;
import com.sb.movie.request.ShowUpdateRequest;
import com.sb.movie.response.SeatAvailabilityResponse;
import com.sb.movie.response.SeatInfo;
import com.sb.movie.response.ShowDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            @CacheEvict(value = "showSearch", allEntries = true),
            @CacheEvict(value = "showsGrouped", allEntries = true),
            @CacheEvict(value = "showDetails", allEntries = true),
            @CacheEvict(value = "eventById", key = "#showRequest.eventId")
    })
    public Show addShow(ShowRequest showRequest) {
        log.info("Adding new show for event ID: {} at theater ID: {}",
                showRequest.getEventId(), showRequest.getTheaterId());

        // Validate that seat prices are provided
        if (showRequest.getPriceOfClassicSeat() == null || showRequest.getPriceOfClassicSeat() <= 0) {
            throw new IllegalArgumentException("Price for classic seats must be provided and greater than 0");
        }
        if (showRequest.getPriceOfPremiumSeat() == null || showRequest.getPriceOfPremiumSeat() <= 0) {
            throw new IllegalArgumentException("Price for premium seats must be provided and greater than 0");
        }

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

        // Check if a show already exists at the same theater, date, and time
        boolean showExists = showRepository.existsByTheaterAndDateAndTime(
                showRequest.getTheaterId(),
                showRequest.getShowDate(),
                showRequest.getShowStartTime()
        );

        if (showExists) {
            throw new ShowAlreadyExistsException(
                    "A show already exists at " + theater.getName() +
                    " on " + showRequest.getShowDate() +
                    " at " + showRequest.getShowStartTime()
            );
        }

        show.setEvent(event);
        show.setTheater(theater);
        show = showRepository.save(show);

        // Create show seats with prices from theater seats
        List<TheaterSeat> theaterSeatList = theater.getTheaterSeatList();
        List<ShowSeat> showSeatList = show.getShowSeatList();

        for (TheaterSeat theaterSeat : theaterSeatList) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setSeatNo(theaterSeat.getSeatNo());
            showSeat.setSeatType(theaterSeat.getSeatType());

            if (showSeat.getSeatType().equals(SeatType.CLASSIC)) {
                showSeat.setPrice(showRequest.getPriceOfClassicSeat());
            } else {
                showSeat.setPrice(showRequest.getPriceOfPremiumSeat());
            }

            showSeat.setShow(show);
            showSeat.setStatus(com.sb.movie.enums.SeatStatus.AVAILABLE);

            showSeatList.add(showSeat);
        }

        Show saved = showRepository.save(show);

        event.getShows().add(saved);
        theater.getShowList().add(saved);

        eventRepository.save(event);
        theaterRepository.save(theater);

        log.info("Show added successfully with ID: {} and {} seats created",
                saved.getShowId(), showSeatList.size());
        return saved;
    }

    @Override
    @Cacheable(value = "showById", key = "#showId", unless = "#result == null")
    public Show getShowById(Integer showId) {
        log.debug("Fetching show by ID from database: {}", showId);
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowDoesNotExists());
    }

    @Override
    @Cacheable(value = "showDetails", key = "#showId", unless = "#result == null")
    public ShowDetailsResponse getShowDetails(Integer showId) throws ShowDoesNotExists {
        log.debug("Fetching show details for show ID: {}", showId);

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowDoesNotExists());

        Event event = show.getEvent();
        Theater theater = show.getTheater();
        Venue venue = theater.getVenue();
        List<ShowSeat> showSeats = show.getShowSeatList();

        // Calculate seat statistics by category
        Map<String, ShowDetailsResponse.SeatCategorySummary> seatSummary = new LinkedHashMap<>();

        Map<SeatType, List<ShowSeat>> seatsByType = showSeats.stream()
                .collect(Collectors.groupingBy(ShowSeat::getSeatType));

        for (Map.Entry<SeatType, List<ShowSeat>> entry : seatsByType.entrySet()) {
            SeatType type = entry.getKey();
            List<ShowSeat> seats = entry.getValue();

            long available = seats.stream()
                    .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                    .count();

            Integer price = seats.isEmpty() ? 0 : (seats.get(0).getPrice() != null ? seats.get(0).getPrice() : 0);

            seatSummary.put(type.name(), ShowDetailsResponse.SeatCategorySummary.builder()
                    .seatType(type.name())
                    .total(seats.size())
                    .available((int) available)
                    .price(price)
                    .build());
        }

        // Calculate overall statistics
        long totalSeats = showSeats.size();
        long availableSeats = showSeats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .count();
        long lockedSeats = showSeats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.LOCKED)
                .count();
        long bookedSeats = showSeats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.BOOKED)
                .count();

        return ShowDetailsResponse.builder()
                .showId(show.getShowId())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .eventId(event.getId())
                .eventName(event.getName())
                .eventType(event.getEventType())
                .duration(event.getDuration())
                .rating(event.getRating())
                .genre(event.getGenre())
                .language(event.getLanguage())
                .description(event.getDescription())
                .posterUrl(event.getPosterUrl())
                .venue(ShowDetailsResponse.VenueInfo.builder()
                        .id(venue.getId())
                        .name(venue.getName())
                        .address(venue.getAddress())
                        .city(venue.getCity())
                        .build())
                .theater(ShowDetailsResponse.TheaterInfo.builder()
                        .id(theater.getId())
                        .name(theater.getName())
                        .build())
                .seatSummary(seatSummary)
                .totalSeats((int) totalSeats)
                .availableSeats((int) availableSeats)
                .lockedSeats((int) lockedSeats)
                .bookedSeats((int) bookedSeats)
                .build();
    }

    @Override
    @Cacheable(value = "showSearch",
               key = "#eventId + '_' + #theaterId + '_' + #date",
               unless = "#result == null || #result.isEmpty()")
    public List<Show> searchShows(Integer eventId, Integer theaterId, Date date) {
        log.debug("Searching shows with filters - eventId: {}, theaterId: {}, date: {}",
                  eventId, theaterId, date);
        return showRepository.searchShows(eventId, theaterId, date);
    }

    @Override
    @Cacheable(value = "showsGrouped", unless = "#result == null || #result.isEmpty()")
    public Map<String, Map<String, List<Show>>> getShowsGroupedByDateAndVenue() {
        log.debug("Fetching shows grouped by date and venue from database");
        List<Show> shows = showRepository.searchShows(null, null, null);

        return shows.stream()
                .collect(Collectors.groupingBy(
                        show -> show.getShowDate().toString(),
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                show -> show.getTheater().getVenue().getName() + " - " + show.getTheater().getName(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ));
    }

    @Override
    @Transactional
    @Caching(
            put = @org.springframework.cache.annotation.CachePut(value = "showById", key = "#showId"),
            evict = {
                    @CacheEvict(value = "showDetails", key = "#showId"),
                    @CacheEvict(value = "showSearch", allEntries = true),
                    @CacheEvict(value = "showsGrouped", allEntries = true)
            }
    )
    public Show updateShow(Integer showId, ShowUpdateRequest showUpdateRequest) throws ShowDoesNotExists {
        log.info("Updating show ID: {}", showId);

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowDoesNotExists());

        // Validate that new date/time is not in the past
        Date newDate = showUpdateRequest.getShowDate();
        java.sql.Time newTime = showUpdateRequest.getShowStartTime();

        LocalDate showDate = newDate.toLocalDate();
        LocalTime showTime = newTime.toLocalTime();
        LocalDateTime newShowDateTime = LocalDateTime.of(showDate, showTime);
        LocalDateTime now = LocalDateTime.now();

        if (newShowDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Cannot update show to a past date/time. Show date/time must be in the future.");
        }

        // Check if another show already exists at the new date/time (excluding current show)
        boolean showExists = showRepository.existsByTheaterAndDateAndTimeExcludingShow(
                show.getTheater().getId(),
                newDate,
                newTime,
                showId
        );

        if (showExists) {
            throw new ShowAlreadyExistsException(
                    "Another show already exists at " + show.getTheater().getName() +
                    " on " + newDate + " at " + newTime
            );
        }

        // Update date and time
        show.setDate(newDate);
        show.setTime(newTime);

        Show updated = showRepository.save(show);
        log.info("Show ID: {} updated successfully and cache updated", showId);
        return updated;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "showById", key = "#showId"),
            @CacheEvict(value = "showDetails", key = "#showId"),
            @CacheEvict(value = "showSearch", allEntries = true),
            @CacheEvict(value = "showsGrouped", allEntries = true)
    })
    public String deleteShow(Integer showId) throws ShowDoesNotExists {
        log.info("Deleting show ID: {}", showId);

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowDoesNotExists());

        if (!show.getTicketList().isEmpty()) {
            throw new RuntimeException("Cannot delete show with existing bookings");
        }

        showRepository.delete(show);
        log.info("Show ID: {} deleted successfully and cache evicted", showId);
        return "Show deleted successfully";
    }

    @Override
    @Cacheable(value = "seatAvailability", key = "#showId", unless = "#result == null")
    public SeatAvailabilityResponse getSeatAvailability(Integer showId) throws ShowDoesNotExists {
        log.debug("Fetching seat availability for show ID: {}", showId);

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowDoesNotExists());

        List<ShowSeat> showSeats = show.getShowSeatList();

        // Calculate seat statistics
        long totalSeats = showSeats.size();
        long availableSeats = showSeats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .count();
        long lockedSeats = showSeats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.LOCKED)
                .count();
        long bookedSeats = showSeats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.BOOKED)
                .count();

        // Convert seats to SeatInfo DTOs
        List<SeatInfo> seatInfoList = showSeats.stream()
                .map(seat -> SeatInfo.builder()
                        .seatNo(seat.getSeatNo())
                        .seatType(seat.getSeatType())
                        .price(seat.getPrice() != null ? seat.getPrice() : 0)
                        .status(seat.getStatus())
                        .build())
                .collect(Collectors.toList());

        return SeatAvailabilityResponse.builder()
                .showId(show.getShowId())
                .showDate(show.getDate())
                .showTime(show.getTime())
                .endTime(show.getEndTime())
                .eventName(show.getEvent().getName())
                .theaterName(show.getTheater().getName())
                .theaterAddress(show.getTheater().getVenue().getAddress())
                .city(show.getTheater().getVenue().getCity())
                .totalSeats((int) totalSeats)
                .availableSeats((int) availableSeats)
                .lockedSeats((int) lockedSeats)
                .bookedSeats((int) bookedSeats)
                .seats(seatInfoList)
                .build();
    }
}