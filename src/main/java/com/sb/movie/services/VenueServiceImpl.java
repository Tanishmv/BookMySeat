package com.sb.movie.services;

import com.sb.movie.converter.VenueConverter;
import com.sb.movie.entities.Venue;
import com.sb.movie.repositories.VenueRepository;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.response.VenueResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenueServiceImpl implements VenueService {

    @Autowired
    private VenueRepository venueRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"venues", "venuesByCity"}, allEntries = true)
    public String addVenue(VenueRequest venueRequest) {
        Venue existingVenue = venueRepository.findByAddress(venueRequest.getAddress());
        if (existingVenue != null) {
            throw new RuntimeException("Venue already exists at this address");
        }

        Venue venue = VenueConverter.venueRequestToVenue(venueRequest);
        venue = venueRepository.save(venue);
        return "Venue has been saved successfully with ID: " + venue.getId();
    }

    @Override
    @Cacheable(value = "venueById", key = "#id")
    public VenueResponse getVenueById(Integer id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + id));
        return VenueConverter.venueToVenueResponse(venue);
    }

    @Override
    @Cacheable(value = "venues")
    public List<VenueResponse> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venues.stream()
                .map(VenueConverter::venueToVenueResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "venuesByCity", key = "#city")
    public List<VenueResponse> getVenuesByCity(String city) {
        List<Venue> venues = venueRepository.findByCityOrderByNameAsc(city);
        return venues.stream()
                .map(VenueConverter::venueToVenueResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"venues", "venueById", "venuesByCity"}, allEntries = true)
    public String updateVenue(Integer id, VenueRequest venueRequest) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + id));

        venue.setName(venueRequest.getName());
        venue.setAddress(venueRequest.getAddress());
        venue.setCity(venueRequest.getCity());
        venue.setDescription(venueRequest.getDescription());

        venueRepository.save(venue);
        return "Venue updated successfully";
    }

    @Override
    @Transactional
    @CacheEvict(value = {"venues", "venueById", "venuesByCity"}, allEntries = true)
    public String deleteVenue(Integer id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + id));

        venueRepository.delete(venue);
        return "Venue deleted successfully";
    }
}
