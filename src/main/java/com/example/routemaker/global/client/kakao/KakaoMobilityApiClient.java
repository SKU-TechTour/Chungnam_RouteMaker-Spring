package com.example.routemaker.global.client.kakao;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class KakaoMobilityApiClient {
    private final RestClient client;
    private final String apiKey;

    public KakaoMobilityApiClient(@Value("${external-api.kakao.mobility-base-url:https://apis-navi.kakaomobility.com}") String baseUrl,
                                  @Value("${external-api.kakao.rest-api-key:}") String apiKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public DrivingRoute directions(double originLongitude, double originLatitude,
                                   double destinationLongitude, double destinationLatitude) {
        requireKey();
        JsonNode root = client.get().uri(builder -> builder.path("/v1/directions")
                        .queryParam("origin", originLongitude + "," + originLatitude)
                        .queryParam("destination", destinationLongitude + "," + destinationLatitude)
                        .queryParam("priority", "RECOMMEND").queryParam("summary", true).build())
                .header("Authorization", "KakaoAK " + apiKey).retrieve().body(JsonNode.class);
        JsonNode route = root == null ? null : root.path("routes").path(0);
        if (route == null || route.isMissingNode() || route.path("result_code").asInt(-1) != 0) {
            throw new IllegalStateException("카카오 길찾기 결과를 찾을 수 없습니다.");
        }
        JsonNode summary = route.path("summary");
        return new DrivingRoute(summary.path("distance").asInt(), summary.path("duration").asInt(),
                summary.path("fare").path("toll").asInt(), summary.path("fare").path("taxi").asInt());
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    private void requireKey() {
        if (!StringUtils.hasText(apiKey)) throw new IllegalStateException("KAKAO_REST_API_KEY 환경변수가 필요합니다.");
    }

    public record DrivingRoute(int distanceMeters, int durationSeconds, int tollWon, int taxiFareWon) {}
}
