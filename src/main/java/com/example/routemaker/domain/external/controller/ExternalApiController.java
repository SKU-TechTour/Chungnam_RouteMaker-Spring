package com.example.routemaker.domain.external.controller;

import com.example.routemaker.domain.place.entity.Place;
import com.example.routemaker.domain.place.repository.PlaceRepository;
import com.example.routemaker.global.client.kakao.KakaoLocalApiClient;
import com.example.routemaker.global.client.kakao.KakaoMobilityApiClient;
import com.example.routemaker.global.client.tour.TourApiClient;
import com.example.routemaker.global.client.tour.TourEnrichmentClient;
import com.example.routemaker.global.client.weather.WeatherApiClient;
import com.example.routemaker.global.common.enums.Region;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
public class ExternalApiController {
    private final TourApiClient tourApiClient;
    private final TourEnrichmentClient tourEnrichmentClient;
    private final WeatherApiClient weatherApiClient;
    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final KakaoMobilityApiClient kakaoMobilityApiClient;
    private final PlaceRepository placeRepository;

    @GetMapping("/tour/search")
    public ApiResponse<?> searchTour(@RequestParam String keyword) {
        return ApiResponse.success(tourApiClient.searchKeyword(keyword));
    }

    @GetMapping("/tour/operating-info")
    public ApiResponse<?> operatingInfo(@RequestParam String contentId, @RequestParam String contentTypeId) {
        return ApiResponse.success(tourApiClient.operatingInfo(contentId, contentTypeId));
    }

    @GetMapping("/tour/common-info")
    public ApiResponse<?> commonInfo(@RequestParam String contentId) {
        return ApiResponse.success(tourApiClient.commonInfo(contentId));
    }

    @GetMapping("/tour/pet-info")
    public ApiResponse<?> petInfo(@RequestParam String contentId) {
        return ApiResponse.success(tourApiClient.petInfo(contentId));
    }

    @GetMapping("/tour/accessibility")
    public ApiResponse<?> accessibility(@RequestParam String contentId) {
        return ApiResponse.success(tourEnrichmentClient.accessibility(contentId));
    }

    @GetMapping("/tour/congestion")
    public ApiResponse<?> congestion(@RequestParam Region region, @RequestParam String attractionName) {
        return ApiResponse.success(tourEnrichmentClient.congestion(region, attractionName));
    }

    @GetMapping("/weather/short-term")
    public ApiResponse<?> weather(@RequestParam String baseDate, @RequestParam String baseTime,
                                  @RequestParam int nx, @RequestParam int ny) {
        return ApiResponse.success(weatherApiClient.shortTerm(baseDate, baseTime, nx, ny));
    }

    @GetMapping("/kakao/places")
    public ApiResponse<?> kakaoPlaces(@RequestParam String query,
                                      @RequestParam(required = false) String categoryGroupCode) {
        return ApiResponse.success(kakaoLocalApiClient.searchKeyword(query, categoryGroupCode));
    }

    @GetMapping("/kakao/driving-route")
    public ApiResponse<?> drivingRoute(@RequestParam long originPlaceId,
                                       @RequestParam long destinationPlaceId) {
        Place origin = placeRepository.findById(originPlaceId)
                .orElseThrow(() -> new IllegalArgumentException("출발 장소를 찾을 수 없습니다."));
        Place destination = placeRepository.findById(destinationPlaceId)
                .orElseThrow(() -> new IllegalArgumentException("도착 장소를 찾을 수 없습니다."));
        return ApiResponse.success(kakaoMobilityApiClient.directions(
                origin.getLongitude(), origin.getLatitude(),
                destination.getLongitude(), destination.getLatitude()));
    }
}
