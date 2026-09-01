package com.example.routemaker.domain.place.dto;

import com.example.routemaker.domain.place.enums.PlaceCategory;
import com.example.routemaker.global.common.enums.Region;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceFilterRequest {

    private Region region;
    private PlaceCategory category;
    private boolean strollerAccessible;
    private boolean petFriendly;
    private boolean largeParking;
    private boolean militaryOnly;
}
