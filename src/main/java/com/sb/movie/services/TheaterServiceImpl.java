package com.sb.movie.services;

import com.sb.movie.converter.TheaterConvertor;
import com.sb.movie.entities.Theater;
import com.sb.movie.entities.TheaterSeat;
import com.sb.movie.enums.SeatType;
import com.sb.movie.exceptions.TheaterIsExist;
import com.sb.movie.exceptions.TheaterIsNotExist;
import com.sb.movie.repositories.TheaterRepository;
import com.sb.movie.request.TheaterRequest;
import com.sb.movie.request.TheaterSeatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheaterServiceImpl implements TheaterService{

    @Autowired
    private TheaterRepository theaterRepository;

    @Override
    public String addTheater(TheaterRequest theaterRequest) throws TheaterIsExist {
        if (theaterRepository.findByAddress(theaterRequest.getAddress()) != null) {
            throw new TheaterIsExist();
        }

        Theater theater = TheaterConvertor.theaterDtoToTheater(theaterRequest);

        theater = theaterRepository.save(theater);
        return "Theater has been saved successfully with ID: " + theater.getId();
    }

    @Override
    public String addTheaterSeat(TheaterSeatRequest entryDto) throws TheaterIsNotExist {
        Theater theater = theaterRepository.findById(entryDto.getTheaterId())
                .orElseThrow(() -> new TheaterIsNotExist());

        Integer noOfSeatsInRow = entryDto.getNoOfSeatInRow();
        Integer noOfPremiumSeats = entryDto.getNoOfPremiumSeat();
        Integer noOfClassicSeat = entryDto.getNoOfClassicSeat();

        List<TheaterSeat> seatList = theater.getTheaterSeatList();

        int counter = 1;
        int fill = 0;
        char ch = 'A';

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

        theaterRepository.save(theater);

        return "Theater Seats have been added successfully";
    }
}
