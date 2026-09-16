package com.example.routemaker.domain.place.service;

import com.example.routemaker.domain.place.dto.PlaceFilterRequest;
import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.global.client.tour.TourApiClient;
import com.example.routemaker.global.client.tour.TourEnrichmentClient;
import com.example.routemaker.global.client.tour.dto.TourOperatingInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import com.example.routemaker.global.common.enums.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final String CHUNGNAM_AREA_CODE = "34";
    private final TourApiClient tourApiClient;
    private final TourEnrichmentClient tourEnrichmentClient;

    public List<PlaceResponse> searchPlaces(PlaceFilterRequest request) {
        Region region = request.getRegion() == null ? Region.NONSAN : request.getRegion();
        if (request.isStrollerAccessible() || request.isMilitaryOnly()) {
            return List.of();
        }

        Set<String> petFriendlyIds = request.isPetFriendly()
                ? tourApiClient.petFriendlyContentIds()
                : Set.of();
        Set<String> accessibleIds = request.isMovementConvenience()
                ? tourEnrichmentClient.accessibleContentIds(region)
                : Set.of();

        return tourApiClient.areaBased(CHUNGNAM_AREA_CODE, sigunguCode(region)).stream()
                .map(place -> enrich(place, region, request, petFriendlyIds))
                .filter(response -> request.getCategory() == null || response.getCategory() == request.getCategory())
                .filter(response -> !request.isPetFriendly() || response.isPetFriendly())
                .filter(response -> !request.isLargeParking() || response.isLargeParking())
                .filter(response -> !request.isMovementConvenience()
                        || accessibleIds.contains(String.valueOf(response.getId())))
                .toList();
    }

    private PlaceResponse enrich(TourPlaceResponse place, Region region, PlaceFilterRequest request,
                                 Set<String> petFriendlyIds) {
        boolean petFriendly = petFriendlyIds.contains(place.contentId());
        boolean largeParking = request.isLargeParking() && hasLargeParking(place);
        return PlaceResponse.fromTour(place, region, petFriendly, largeParking);
    }

    private boolean hasLargeParking(TourPlaceResponse place) {
        try {
            TourOperatingInfoResponse info = tourApiClient.operatingInfo(place.contentId(), place.contentTypeId());
            return info.parking().contains("대형");
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private String sigunguCode(Region region) {
        return switch (region) {
            case GONGJU -> "1";
            case NONSAN -> "3";
            case BUYEO -> "6";
        };
    }
}
