package com.sb.movie.services;

import com.sb.movie.converter.TheaterConvertor;
import com.sb.movie.entities.Theater;
import com.sb.movie.entities.TheaterSeat;
import com.sb.movie.entities.Venue;
import com.sb.movie.enums.SeatType;
import com.sb.movie.exceptions.TheaterIsExist;
import com.sb.movie.exceptions.TheaterIsNotExist;
import com.sb.movie.repositories.TheaterRepository;
import com.sb.movie.repositories.VenueRepository;
import com.sb.movie.request.TheaterRequest;
import com.sb.movie.request.TheaterUpdateRequest;
import com.sb.movie.response.TheaterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheaterServiceImpl implements TheaterService{

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Override
    @Transactional
    public String addTheater(TheaterRequest theaterRequest) throws TheaterIsExist {
        Theater theater = TheaterConvertor.theaterDtoToTheater(theaterRequest);

        // Fetch and set venue (required)
        Venue venue = venueRepository.findById(theaterRequest.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + theaterRequest.getVenueId()));
        theater.setVenue(venue);

        // Create seats automatically
        Integer noOfSeatsInRow = theaterRequest.getNoOfSeatInRow();
        Integer noOfPremiumSeats = theaterRequest.getNoOfPremiumSeat();
        Integer noOfClassicSeat = theaterRequest.getNoOfClassicSeat();

        List<TheaterSeat> seatList = theater.getTheaterSeatList();

        int counter = 1;
        int fill = 0;
        char ch = 'A';

        // Add classic seats
        for (int i = 1; i <= noOfClassicSeat; i++) {
            String seatNo = Integer.toString(counter) + ch;

            ch++;
            fill++;
            if (fill == noOfSeatsInRow) {
                fill = 0;
                counter++;
                ch = 'A';
            }

            TheaterSeat theaterSeat = new TheaterSeat();
            theaterSeat.setSeatNo(seatNo);
            theaterSeat.setSeatType(SeatType.CLASSIC);
            theaterSeat.setTheater(theater);
            seatList.add(theaterSeat);
        }

        // Add premium seats
        for (int i = 1; i <= noOfPremiumSeats; i++) {
            String seatNo = Integer.toString(counter) + ch;

            ch++;
            fill++;
            if (fill == noOfSeatsInRow) {
                fill = 0;
                counter++;
                ch = 'A';
            }

            TheaterSeat theaterSeat = new TheaterSeat();
            theaterSeat.setSeatNo(seatNo);
            theaterSeat.setSeatType(SeatType.PREMIUM);
            theaterSeat.setTheater(theater);
            seatList.add(theaterSeat);
        }

        theater = theaterRepository.save(theater);
        return "Theater has been saved successfully with ID: " + theater.getId() +
               " with " + seatList.size() + " seats";
    }

    @Override
    public List<TheaterResponse> getAllTheaters() {
        List<Theater> theaters = theaterRepository.findAll();
        return theaters.stream()
                .map(TheaterConvertor::theaterToTheaterResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TheaterResponse getTheaterById(Integer id) throws TheaterIsNotExist {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new TheaterIsNotExist());
        return TheaterConvertor.theaterToTheaterResponse(theater);
    }

    @Override
    public List<TheaterResponse> getTheatersByCity(String city) {
        List<Theater> theaters = theaterRepository.findByCity(city);
        return theaters.stream()
                .map(TheaterConvertor::theaterToTheaterResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String updateTheater(Integer id, TheaterUpdateRequest theaterUpdateRequest) throws TheaterIsNotExist {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new TheaterIsNotExist());

        // Update only the name - venue and seat configuration cannot be changed
        // A theater is physically located at a venue and cannot be moved
        theater.setName(theaterUpdateRequest.getName());

        theaterRepository.save(theater);
        return "Theater updated successfully";
    }

    @Override
    @Transactional
    public String deleteTheater(Integer id) throws TheaterIsNotExist {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new TheaterIsNotExist());

        theaterRepository.delete(theater);
        return "Theater deleted successfully";
    }
}
