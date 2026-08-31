package com.example.routemaker.domain.military.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MilitaryBranch {
    ARMY("육군"),
    NAVY("해군"),
    AIR_FORCE("공군"),
    MARINE_CORPS("해병대");

    private final String displayName;
}
