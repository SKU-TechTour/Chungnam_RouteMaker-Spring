package com.example.routemaker.domain.course.entity;

import com.example.routemaker.domain.place.entity.Place;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "course_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private int stepOrder;

    @Builder
    public CourseItem(Course course, Place place, int stepOrder) {
        this.course = course;
        this.place = place;
        this.stepOrder = stepOrder;
    }
}
