package com.example.routemaker.domain.military.entity;

import com.example.routemaker.domain.military.enums.MilitaryBranch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "enlistment_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnlistmentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_code", nullable = false, unique = true, length = 40)
    private String scheduleCode;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "military_branch", nullable = false, length = 30)
    private MilitaryBranch militaryBranch;

    @Column(name = "recruitment_type", nullable = false, length = 60)
    private String recruitmentType;

    @Column(name = "application_start_date", nullable = false)
    private LocalDate applicationStartDate;

    @Column(name = "application_end_date", nullable = false)
    private LocalDate applicationEndDate;

    @Column(name = "enlistment_date", nullable = false)
    private LocalDate enlistmentDate;

    @Column(name = "training_center", nullable = false, length = 120)
    private String trainingCenter;

    @Column(nullable = false, length = 30)
    private String region;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
