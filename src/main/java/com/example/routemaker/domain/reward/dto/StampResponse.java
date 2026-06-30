package com.example.routemaker.domain.reward.dto;

import com.example.routemaker.domain.reward.entity.Stamp;
import com.example.routemaker.global.common.enums.Region;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StampResponse {

    private Long id;
    private String name;
    private Region region;

    public static StampResponse from(Stamp stamp) {
        return StampResponse.builder()
                .id(stamp.getId())
                .name(stamp.getName())
                .region(stamp.getRegion())
                .build();
    }
}
