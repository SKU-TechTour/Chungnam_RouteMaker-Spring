package com.example.routemaker.domain.military.dto;

import com.example.routemaker.domain.military.entity.EnlistmentSchedule;

import java.time.LocalDate;

public record EnlistmentScheduleResponse(
        Long id,
        String scheduleCode,
        String title,
        String militaryBranch,
        String militaryBranchName,
        String recruitmentType,
        LocalDate applicationStartDate,
        LocalDate applicationEndDate,
        LocalDate enlistmentDate,
        String trainingCenter,
        String region,
        String sourceUrl
) {
    public static EnlistmentScheduleResponse from(EnlistmentSchedule schedule) {
        return new EnlistmentScheduleResponse(
                schedule.getId(),
                schedule.getScheduleCode(),
                schedule.getTitle(),
                schedule.getMilitaryBranch().name(),
                schedule.getMilitaryBranch().getDisplayName(),
                schedule.getRecruitmentType(),
                schedule.getApplicationStartDate(),
                schedule.getApplicationEndDate(),
                schedule.getEnlistmentDate(),
                schedule.getTrainingCenter(),
                schedule.getRegion(),
                schedule.getSourceUrl()
        );
    }
}
