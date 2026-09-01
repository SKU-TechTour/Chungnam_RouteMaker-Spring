package com.example.routemaker.domain.place.dto;

import com.example.routemaker.domain.place.entity.Place;
import com.example.routemaker.domain.place.enums.PlaceCategory;
import com.example.routemaker.global.common.enums.Region;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
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
    private String imageUrl;
    private String source;

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

    public static PlaceResponse fromTour(TourPlaceResponse place, Region region,
                                         boolean petFriendly, boolean largeParking) {
        return PlaceResponse.builder()
                .id(Long.parseLong(place.contentId()))
                .name(place.name())
                .region(region)
                .category(categoryOf(place))
                .address(place.address())
                .latitude(place.latitude())
                .longitude(place.longitude())
                .strollerAccessible(false)
                .petFriendly(petFriendly)
                .largeParking(largeParking)
                .militaryDiscount(false)
                .imageUrl(place.imageUrl())
                .source("TOUR_API_REALTIME")
                .build();
    }

    public static PlaceResponse nonsanTrainingCenter() {
        return PlaceResponse.builder()
                .id(-1L)
                .name("육군훈련소")
                .region(Region.NONSAN)
                .category(PlaceCategory.HERITAGE)
                .address("충남 논산시 연무읍 득안대로 504")
                .latitude(36.1119731)
                .longitude(127.1083526)
                .largeParking(true)
                .source("SERVICE_ANCHOR")
                .build();
    }

    private static PlaceCategory categoryOf(TourPlaceResponse place) {
        if ("32".equals(place.contentTypeId())) return PlaceCategory.ACCOMMODATION;
        if ("39".equals(place.contentTypeId())) {
            return "A05020900".equals(place.categoryCode()) ? PlaceCategory.CAFE : PlaceCategory.RESTAURANT;
        }
        return PlaceCategory.HERITAGE;
    }
}
