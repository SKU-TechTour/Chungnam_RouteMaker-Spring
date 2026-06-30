package com.example.routemaker.domain.place.dto;

import com.example.routemaker.domain.place.entity.Place;
import com.example.routemaker.domain.place.enums.PlaceCategory;
import com.example.routemaker.global.common.enums.Region;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceResponse {

    private Long id;
    private String name;
    private Region region;
    private PlaceCategory category;
    private String address;
    private double latitude;
    private double longitude;
    private boolean strollerAccessible;
    private boolean petFriendly;
    private boolean largeParking;
    private boolean militaryDiscount;

    public static PlaceResponse from(Place place) {
        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .region(place.getRegion())
                .category(place.getCategory())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .strollerAccessible(place.isStrollerAccessible())
                .petFriendly(place.isPetFriendly())
                .largeParking(place.isLargeParking())
                .militaryDiscount(place.isMilitaryDiscount())
                .build();
    }
}
