package com.example.routemaker.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    private String nickname;
    private boolean military;
    private LocalDate trainingStartDate;
    private Long militaryUnitId;
}
