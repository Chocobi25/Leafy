package com.chocobi.leafy.trip.entity;

import com.chocobi.leafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private LocalDate start_date;
    private LocalDate end_date;

    @Builder.Default
    private double carbonSaved = 0.0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TripStatus status = TripStatus.DRAFT;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripPlace> tripPlaces = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.start_date = startDate;
        this.end_date = endDate;
    }
}
