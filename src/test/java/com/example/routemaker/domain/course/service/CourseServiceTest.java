package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.dto.HourlyWeatherResponse;
import com.example.routemaker.domain.course.dto.RoutePointRequest;
import com.example.routemaker.domain.course.dto.RoutePreviewRequest;
import com.example.routemaker.global.client.kakao.KakaoMobilityApiClient;
import com.example.routemaker.global.client.tour.TourApiClient;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import com.example.routemaker.global.client.weather.WeatherApiClient;
import com.example.routemaker.global.common.enums.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock TourApiClient tourApiClient;
    @Mock WeatherApiClient weatherApiClient;
    @Mock KakaoMobilityApiClient kakaoMobilityApiClient;
    @InjectMocks CourseService courseService;

    @Test
    void combinesRealtimeTourWeatherAndKakaoData() {
        CourseRecommendRequest request = new CourseRecommendRequest();
        request.setRegion(Region.GONGJU);
        request.setConcepts(Set.of("history", "food", "cafe"));

        TourPlaceResponse attraction = place("100", "12", "A02010100", "공산성", 127.1, 36.4);
        TourPlaceResponse restaurant = place("200", "39", "A05020100", "공주식당", 127.2, 36.41);
        TourPlaceResponse cafe = place("300", "39", "A05020900", "공주카페", 127.3, 36.42);

        when(weatherApiClient.hourly("GONGJU")).thenReturn(List.of(
                new HourlyWeatherResponse("12:00", 24, 10, false),
                new HourlyWeatherResponse("15:00", 25, 20, false)
        ));
        when(tourApiClient.areaBased("34", "1", "12", 80)).thenReturn(List.of(attraction));
        when(tourApiClient.areaBased("34", "1", "39", 100)).thenReturn(List.of(restaurant, cafe));
        when(kakaoMobilityApiClient.directions(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new KakaoMobilityApiClient.DrivingRoute(5000, 600, 0, 8000));

        CourseResponse response = courseService.recommendCourse(request);

        assertThat(response.getCombo()).extracting("name")
                .containsExactly("공산성", "공주식당", "공주카페");
        assertThat(response.getCombo()).extracting("source")
                .containsOnly("TOUR_API_REALTIME");
        assertThat(response.getRoutes()).hasSize(2);
        assertThat(response.getTotalDistanceMeters()).isEqualTo(10000);
        assertThat(response.getTotalDurationSeconds()).isEqualTo(1200);
        assertThat(response.getHourlyWeather()).hasSize(2);
        assertThat(response.getSource())
                .isEqualTo("TOUR_API_REALTIME+WEATHER_API_REALTIME+KAKAO_MOBILITY_REALTIME");
    }

    @Test
    void fixesTrainingCenterAsSecondStopAtOnePm() {
        CourseRecommendRequest request = new CourseRecommendRequest();
        request.setRegion(Region.NONSAN);
        request.setMilitary(true);

        TourPlaceResponse attraction = place("101", "12", "A02010100", "논산 명소", 127.12, 36.12);
        TourPlaceResponse restaurant = place("201", "39", "A05020100", "논산 식당", 127.13, 36.13);
        TourPlaceResponse cafe = place("301", "39", "A05020900", "논산 카페", 127.14, 36.14);

        when(weatherApiClient.hourly("NONSAN")).thenReturn(List.of());
        when(tourApiClient.areaBased("34", "3", "12", 80)).thenReturn(List.of(attraction));
        when(tourApiClient.areaBased("34", "3", "39", 100)).thenReturn(List.of(restaurant, cafe));
        when(kakaoMobilityApiClient.directions(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new KakaoMobilityApiClient.DrivingRoute(3000, 420, 0, 0));

        CourseResponse response = courseService.recommendCourse(request);

        assertThat(response.getCombo()).extracting("name")
                .containsExactly("논산 명소", "육군훈련소", "논산 식당");
        assertThat(response.getCombo().get(1).getScheduledTime()).isEqualTo("13:00 도착");
    }

    @Test
    void buildsEnlisteeDirectRouteWithTrainingCenterLast() {
        CourseRecommendRequest request = new CourseRecommendRequest();
        request.setRegion(Region.NONSAN);
        request.setMilitary(true);
        request.setRouteTemplate("ENLISTEE_B");

        when(weatherApiClient.hourly("NONSAN")).thenReturn(List.of());
        when(tourApiClient.areaBased("34", "3", "12", 80))
                .thenReturn(List.of(place("100", "12", "A02010100", "돈암서원", 127.1, 36.2)));
        when(tourApiClient.areaBased("34", "3", "39", 100))
                .thenReturn(List.of(place("200", "39", "A05020100", "연무식당", 127.11, 36.15)));
        when(kakaoMobilityApiClient.directions(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new KakaoMobilityApiClient.DrivingRoute(3000, 420, 0, 0));

        CourseResponse response = courseService.recommendCourse(request);

        assertThat(response.getCombo()).extracting("name")
                .containsExactly("연무식당", "육군훈련소");
        assertThat(response.getCombo().get(1).getScheduledTime()).isEqualTo("13:00 도착");
        assertThat(response.getRoutes()).hasSize(1);
    }

    @Test
    void buildsVariableLengthCompanionDayRoute() {
        CourseRecommendRequest request = new CourseRecommendRequest();
        request.setRegion(Region.NONSAN);
        request.setMilitary(true);
        request.setRouteTemplate("COMPANION_DAY_A");

        TourPlaceResponse heritage1 = place("100", "12", "A02010100", "돈암서원", 127.1, 36.2);
        TourPlaceResponse heritage2 = place("101", "12", "A02010100", "선샤인랜드", 127.2, 36.3);
        TourPlaceResponse restaurant1 = place("200", "39", "A05020100", "논산식당", 127.11, 36.15);
        TourPlaceResponse restaurant2 = place("201", "39", "A05020100", "연무식당", 127.12, 36.16);
        TourPlaceResponse cafe = place("300", "39", "A05020900", "탑정호카페", 127.15, 36.18);
        when(weatherApiClient.hourly("NONSAN")).thenReturn(List.of());
        when(tourApiClient.areaBased("34", "3", "12", 80)).thenReturn(List.of(heritage1, heritage2));
        when(tourApiClient.areaBased("34", "3", "39", 100)).thenReturn(List.of(restaurant1, restaurant2, cafe));
        when(kakaoMobilityApiClient.directions(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new KakaoMobilityApiClient.DrivingRoute(2000, 300, 0, 0));

        CourseResponse response = courseService.recommendCourse(request);

        assertThat(response.getCombo()).hasSize(6);
        assertThat(response.getCombo().get(1).getName()).isEqualTo("육군훈련소");
        assertThat(response.getRoutes()).hasSize(5);
    }

    @Test
    void recalculatesTwoRouteLegsForThreeSelectedPlaces() {
        when(kakaoMobilityApiClient.detailedDirections(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new KakaoMobilityApiClient.DrivingRoute(
                        2500, 300, 0, 0,
                        List.of(new KakaoMobilityApiClient.RouteCoordinate(36.1, 127.1)),
                        List.of(new KakaoMobilityApiClient.RouteGuide(
                                "오른쪽 방향", 36.1, 127.1, 300, 40))));

        var response = courseService.previewRoute(new RoutePreviewRequest(List.of(
                new RoutePointRequest(1L, 36.1, 127.1),
                new RoutePointRequest(2L, 36.2, 127.2),
                new RoutePointRequest(3L, 36.3, 127.3)
        )));

        assertThat(response.routes()).hasSize(2);
        assertThat(response.totalDistanceMeters()).isEqualTo(5000);
        assertThat(response.totalDurationSeconds()).isEqualTo(600);
        assertThat(response.routes().get(0).getPath()).hasSize(1);
        assertThat(response.routes().get(0).getGuides().get(0).instruction())
                .isEqualTo("오른쪽 방향");
    }

    @Test
    void keepsCourseAvailableWhenWeatherAndKakaoTemporarilyFail() {
        CourseRecommendRequest request = new CourseRecommendRequest();
        request.setRegion(Region.GONGJU);

        when(weatherApiClient.hourly("GONGJU"))
                .thenThrow(new IllegalStateException("weather timeout"));
        when(tourApiClient.areaBased("34", "1", "12", 80))
                .thenReturn(List.of(place("100", "12", "A02010100", "공산성", 127.1, 36.4)));
        when(tourApiClient.areaBased("34", "1", "39", 100))
                .thenReturn(List.of(
                        place("200", "39", "A05020100", "공주식당", 127.2, 36.41),
                        place("300", "39", "A05020900", "공주카페", 127.3, 36.42)));
        when(kakaoMobilityApiClient.directions(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new IllegalStateException("mobility timeout"));

        CourseResponse response = courseService.recommendCourse(request);

        assertThat(response.getCombo()).hasSize(3);
        assertThat(response.getHourlyWeather()).isEmpty();
        assertThat(response.getRoutes()).allMatch(route ->
                "LOCAL_DISTANCE_FALLBACK".equals(route.getSource()));
        assertThat(response.getTotalDistanceMeters()).isPositive();
    }

    private TourPlaceResponse place(String id, String type, String category, String name,
                                    double longitude, double latitude) {
        return new TourPlaceResponse(id, type, category, name, "충남", "", longitude, latitude);
    }
}
