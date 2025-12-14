package com.sb.movie.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sb.movie.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private Integer age;

    private String address;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    private String mobileNo;

    @Column(unique = true)
    private String emailId;

    @Column(length = 100, nullable = false)
    private String password;

    private String roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ticket> ticketList = new ArrayList<>();
}
