package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.dto.RouteLegResponse;
import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.global.client.kakao.KakaoMobilityApiClient;
import com.example.routemaker.global.client.tour.TourApiClient;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import com.example.routemaker.global.client.weather.WeatherApiClient;
import com.example.routemaker.global.common.enums.Region;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final String CHUNGNAM_AREA_CODE = "34";
    private static final String TOURIST_ATTRACTION = "12";
    private static final String CULTURAL_FACILITY = "14";
    private static final String RESTAURANT = "39";

    private final TourApiClient tourApiClient;
    private final WeatherApiClient weatherApiClient;
    private final KakaoMobilityApiClient kakaoMobilityApiClient;

    public CourseResponse recommendCourse(CourseRecommendRequest request) {
        if (request.getRegion() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지역을 선택해야 합니다.");
        }
        return compose(request.getRegion(), request.isMilitary(), request.getConcepts(), request.getVariant());
    }

    public CourseResponse shufflePlanB(Long courseId) {
        int regionCode = Math.toIntExact(courseId / 1000);
        if (regionCode < 1 || regionCode > Region.values().length) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "코스 ID가 올바르지 않습니다.");
        }
        Region region = Region.values()[regionCode - 1];
        int nextVariant = Math.toIntExact(courseId % 1000) + 1;
        return compose(region, false, Set.of(), nextVariant);
    }

    private CourseResponse compose(Region region, boolean military, Set<String> concepts, int variant) {
        boolean rainy = weatherApiClient.isRainy(region.name());
        String attractionType = rainy ? CULTURAL_FACILITY : TOURIST_ATTRACTION;

        List<TourPlaceResponse> attractions = tourApiClient.areaBased(
                CHUNGNAM_AREA_CODE, sigunguCode(region), attractionType, 80);
        if (attractions.isEmpty()) {
            attractions = tourApiClient.areaBased(
                    CHUNGNAM_AREA_CODE, sigunguCode(region), TOURIST_ATTRACTION, 80);
        }
        List<TourPlaceResponse> dining = tourApiClient.areaBased(
                CHUNGNAM_AREA_CODE, sigunguCode(region), RESTAURANT, 100);

        TourPlaceResponse attraction = pick(attractions, variant, "관광지");
        List<TourPlaceResponse> cafes = dining.stream().filter(this::isCafe).toList();
        List<TourPlaceResponse> restaurants = dining.stream().filter(place -> !isCafe(place)).toList();
        TourPlaceResponse restaurant = pick(restaurants.isEmpty() ? dining : restaurants, variant, "맛집");
        TourPlaceResponse cafe = pick(cafes.isEmpty() ? dining : cafes, variant + 1, "카페");

        List<PlaceResponse> combo = new ArrayList<>();
        combo.add(region == Region.NONSAN && military
                ? PlaceResponse.nonsanTrainingCenter()
                : PlaceResponse.fromTour(attraction, region, false, false));
        combo.add(PlaceResponse.fromTour(restaurant, region, false, false));
        combo.add(PlaceResponse.fromTour(cafe, region, false, false));

        List<RouteLegResponse> routes = buildRoutes(combo);
        int totalDistance = routes.stream().mapToInt(RouteLegResponse::getDistanceMeters).sum();
        int totalDuration = routes.stream().mapToInt(RouteLegResponse::getDurationSeconds).sum();
        long id = (region.ordinal() + 1L) * 1000L + Math.floorMod(variant, 1000);

        return CourseResponse.builder()
                .id(id)
                .title(title(region, military, rainy, concepts))
                .region(region)
                .indoor(rainy)
                .weather(rainy ? "RAINY" : "CLEAR")
                .combo(List.copyOf(combo))
                .routes(routes)
                .totalDistanceMeters(totalDistance)
                .totalDurationSeconds(totalDuration)
                .source("TOUR_API_REALTIME+WEATHER_API_REALTIME+KAKAO_MOBILITY_REALTIME")
                .build();
    }

    private List<RouteLegResponse> buildRoutes(List<PlaceResponse> combo) {
        List<RouteLegResponse> routes = new ArrayList<>();
        for (int index = 0; index < combo.size() - 1; index++) {
            PlaceResponse origin = combo.get(index);
            PlaceResponse destination = combo.get(index + 1);
            KakaoMobilityApiClient.DrivingRoute route = kakaoMobilityApiClient.directions(
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude());
            routes.add(RouteLegResponse.builder()
                    .originPlaceId(origin.getId())
                    .destinationPlaceId(destination.getId())
                    .distanceMeters(route.distanceMeters())
                    .durationSeconds(route.durationSeconds())
                    .tollWon(route.tollWon())
                    .taxiFareWon(route.taxiFareWon())
                    .source("KAKAO_MOBILITY_REALTIME")
                    .build());
        }
        return List.copyOf(routes);
    }

    private TourPlaceResponse pick(List<TourPlaceResponse> places, int variant, String label) {
        List<TourPlaceResponse> usable = places.stream()
                .filter(place -> place.latitude() != 0 && place.longitude() != 0)
                .toList();
        if (usable.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, label + " 실시간 관광정보를 찾을 수 없습니다.");
        }
        return usable.get(Math.floorMod(variant, usable.size()));
    }

    private boolean isCafe(TourPlaceResponse place) {
        String name = place.name().toLowerCase();
        return "A05020900".equals(place.categoryCode())
                || name.contains("카페") || name.contains("커피")
                || name.contains("베이커리") || name.contains("빵");
    }

    private String title(Region region, boolean military, boolean rainy, Set<String> concepts) {
        String regionName = switch (region) {
            case NONSAN -> "논산";
            case GONGJU -> "공주";
            case BUYEO -> "부여";
        };
        if (region == Region.NONSAN && military) {
            return "육군훈련소 입영 전 마음 정리 3단 콤보";
        }
        if (rainy) {
            return regionName + " 비 오는 날 실내 3단 콤보";
        }
        return concepts != null && concepts.contains("history")
                ? regionName + " 역사 중심 3단 콤보"
                : regionName + " 취향 맞춤 3단 콤보";
    }

    private String sigunguCode(Region region) {
        return switch (region) {
            case GONGJU -> "1";
            case NONSAN -> "3";
            case BUYEO -> "6";
        };
    }
}
