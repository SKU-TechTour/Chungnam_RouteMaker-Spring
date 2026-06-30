package com.example.routemaker.domain.place.service;

import com.example.routemaker.domain.place.dto.PlaceFilterRequest;
import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.domain.place.entity.Place;
import com.example.routemaker.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public List<PlaceResponse> searchPlaces(PlaceFilterRequest request) {
        List<PlaceResponse> places = placeRepository.findAll().stream()
                .filter(place -> matchesRegion(place, request))
                .filter(place -> matchesCategory(place, request))
                .filter(place -> matchesFilters(place, request))
                .sorted(militaryDiscountFirst(request.isMilitaryOnly()))
                .map(PlaceResponse::from)
                .toList();

        return places;
    }

    private boolean matchesRegion(Place place, PlaceFilterRequest request) {
        return request.getRegion() == null || place.getRegion() == request.getRegion();
    }

    private boolean matchesCategory(Place place, PlaceFilterRequest request) {
        return request.getCategory() == null || place.getCategory() == request.getCategory();
    }

    private boolean matchesFilters(Place place, PlaceFilterRequest request) {
        if (request.isStrollerAccessible() && !place.isStrollerAccessible()) {
            return false;
        }
        if (request.isPetFriendly() && !place.isPetFriendly()) {
            return false;
        }
        if (request.isLargeParking() && !place.isLargeParking()) {
            return false;
        }
        if (request.isMilitaryOnly() && !place.isMilitaryDiscount()) {
            return false;
        }
        return true;
    }

    private Comparator<Place> militaryDiscountFirst(boolean militaryOnly) {
        if (!militaryOnly) {
            return Comparator.comparing(Place::isMilitaryDiscount).reversed();
        }
        return Comparator.comparing(Place::getName);
    }
}
