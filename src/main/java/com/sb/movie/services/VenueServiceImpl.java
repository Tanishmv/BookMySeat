package com.sb.movie.services;

import com.sb.movie.converter.VenueConverter;
import com.sb.movie.entities.Venue;
import com.sb.movie.repositories.VenueRepository;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.request.VenueUpdateRequest;
import com.sb.movie.response.VenueResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    public VenueResponse addVenue(VenueRequest venueRequest) {
        Venue existingVenue = venueRepository.findByAddress(venueRequest.getAddress());
        if (existingVenue != null) {
            throw new RuntimeException("Venue already exists at this address");
        }

        Venue venue = VenueConverter.venueRequestToVenue(venueRequest);
        Venue saved = venueRepository.save(venue);
        return VenueConverter.venueToVenueResponse(saved);
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
    @Caching(
            put = @CachePut(value = "venueById", key = "#id"),
            evict = {
                    @CacheEvict(value = "venues", allEntries = true),
                    @CacheEvict(value = "venuesByCity", allEntries = true)
            }
    )
    public VenueResponse updateVenue(Integer id, VenueUpdateRequest venueUpdateRequest) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + id));

        // Only update fields that are not null
        if (venueUpdateRequest.getName() != null) {
            venue.setName(venueUpdateRequest.getName());
        }
        if (venueUpdateRequest.getAddress() != null) {
            venue.setAddress(venueUpdateRequest.getAddress());
        }
        if (venueUpdateRequest.getCity() != null) {
            venue.setCity(venueUpdateRequest.getCity());
        }
        if (venueUpdateRequest.getDescription() != null) {
            venue.setDescription(venueUpdateRequest.getDescription());
        }

        Venue updated = venueRepository.save(venue);
        return VenueConverter.venueToVenueResponse(updated);
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
