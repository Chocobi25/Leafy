package com.chocobi.leafy.trip.entity;

import com.chocobi.leafy.place.entity.RegionGroup;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private RegionGroup departure;

    @Enumerated(EnumType.STRING)
    private RegionGroup arrival;

    @Builder.Default
    private double carbonSaved = 0.0;

    @Builder.Default
    private double carbonEmission = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private TripStatus status = TripStatus.CREATING;

    private LocalDateTime certificationAt;

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
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void certify() {
        if (this.certificationAt != null) {
            throw new IllegalStateException("이미 위치 인증을 완료했습니다.");
        }
        this.certificationAt = LocalDateTime.now();
    }

    public void editStatus(TripStatus status) {
        this.status = status;
    }
}
