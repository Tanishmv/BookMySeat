package com.sb.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {

    private Integer id;
    private String name;
    private String address;
    private String city;
    private String description;
    private Integer totalTheaters;
    private List<TheaterInfo> theaters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TheaterInfo {
        private Integer id;
        private String name;
    }
}
