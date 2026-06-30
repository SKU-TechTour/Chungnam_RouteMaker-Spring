package com.example.routemaker.domain.military.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SafeTimeResponse {

    private String unitName;
    private int remainingMinutes;
    private int travelMinutes;
    private boolean safe;
}
