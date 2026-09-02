package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.dto.HourlyWeatherResponse;
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

    private TourPlaceResponse place(String id, String type, String category, String name,
                                    double longitude, double latitude) {
        return new TourPlaceResponse(id, type, category, name, "충남", "", longitude, latitude);
    }
}
