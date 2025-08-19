package com.chocobi.leafy.trip.entity;

import com.chocobi.leafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Trip implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    private User user;

    private LocalDate start_date;
    private LocalDate end_date;

    private double carbon_saved;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripPlace> tripPlaces = new ArrayList<>();

    public void update(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.start_date = startDate;
        this.end_date = endDate;
    }
}
