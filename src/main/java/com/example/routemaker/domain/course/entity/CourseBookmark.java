package com.example.routemaker.domain.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "course_bookmarks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_bookmark_user_route",
                columnNames = {"firebase_uid", "route_key"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "route_key", nullable = false)
    private String routeKey;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String title;

    @Column(name = "spots_json", nullable = false, columnDefinition = "TEXT")
    private String spotsJson;

    @Column(name = "total_distance_meters", nullable = false)
    private int totalDistanceMeters;

    @Column(name = "total_duration_seconds", nullable = false)
    private int totalDurationSeconds;

    @Builder
    public CourseBookmark(
            String firebaseUid,
            String routeKey,
            String region,
            String title,
            String spotsJson,
            int totalDistanceMeters,
            int totalDurationSeconds
    ) {
        this.firebaseUid = firebaseUid;
        this.routeKey = routeKey;
        this.region = region;
        this.title = title;
        this.spotsJson = spotsJson;
        this.totalDistanceMeters = totalDistanceMeters;
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public void updateSnapshot(
            String region,
            String title,
            String spotsJson,
            int totalDistanceMeters,
            int totalDurationSeconds
    ) {
        this.region = region;
        this.title = title;
        this.spotsJson = spotsJson;
        this.totalDistanceMeters = totalDistanceMeters;
        this.totalDurationSeconds = totalDurationSeconds;
    }
}
