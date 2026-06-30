package com.example.routemaker.domain.user.entity;

import com.example.routemaker.global.common.enums.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    private String courseTitle;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Builder
    public TravelRecord(User user, Region region, String courseTitle, LocalDateTime completedAt) {
        this.user = user;
        this.region = region;
        this.courseTitle = courseTitle;
        this.completedAt = completedAt;
    }
}
