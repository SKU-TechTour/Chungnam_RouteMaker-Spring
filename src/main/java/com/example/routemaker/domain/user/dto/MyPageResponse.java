package com.example.routemaker.domain.user.dto;

import com.example.routemaker.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MyPageResponse {

    private Long userId;
    private String email;
    private String nickname;
    private boolean military;
    private LocalDate trainingStartDate;
    private Long militaryUnitId;
    private List<TravelRecordSummary> travelRecords;

    @Getter
    @Builder
    public static class TravelRecordSummary {
        private Long id;
        private String region;
        private String courseTitle;
        private String completedAt;
    }

    public static MyPageResponse from(User user, List<TravelRecordSummary> travelRecords) {
        return MyPageResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .military(user.isMilitary())
                .trainingStartDate(user.getTrainingStartDate())
                .militaryUnitId(user.getMilitaryUnitId())
                .travelRecords(travelRecords)
                .build();
    }
}
