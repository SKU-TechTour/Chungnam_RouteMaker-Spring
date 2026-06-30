package com.example.routemaker.domain.reward.dto;

import com.example.routemaker.domain.reward.entity.Badge;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeResponse {

    private Long id;
    private String name;
    private String description;

    public static BadgeResponse from(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .build();
    }
}
