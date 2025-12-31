package com.sb.movie.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VENUES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String address;

    @Column(nullable = false)
    private String city;

    private String description;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Theater> theaters = new ArrayList<>();
}
