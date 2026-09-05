package com.example.routemaker.global.client.kakao;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

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
        return directions(originLongitude, originLatitude, destinationLongitude, destinationLatitude, false);
    }

    public DrivingRoute detailedDirections(double originLongitude, double originLatitude,
                                           double destinationLongitude, double destinationLatitude) {
        return directions(originLongitude, originLatitude, destinationLongitude, destinationLatitude, true);
    }

    private DrivingRoute directions(double originLongitude, double originLatitude,
                                    double destinationLongitude, double destinationLatitude,
                                    boolean includeDetails) {
        requireKey();
        JsonNode root = client.get().uri(builder -> builder.path("/v1/directions")
                        .queryParam("origin", originLongitude + "," + originLatitude)
                        .queryParam("destination", destinationLongitude + "," + destinationLatitude)
                        .queryParam("priority", "RECOMMEND")
                        .queryParam("summary", !includeDetails).build())
                .header("Authorization", "KakaoAK " + apiKey).retrieve().body(JsonNode.class);
        JsonNode route = root == null ? null : root.path("routes").path(0);
        if (route == null || route.isMissingNode() || route.path("result_code").asInt(-1) != 0) {
            throw new IllegalStateException("카카오 길찾기 결과를 찾을 수 없습니다.");
        }
        JsonNode summary = route.path("summary");
        List<RouteCoordinate> path = new ArrayList<>();
        List<RouteGuide> guides = new ArrayList<>();
        if (includeDetails) {
            for (JsonNode section : route.path("sections")) {
                for (JsonNode road : section.path("roads")) {
                    JsonNode vertexes = road.path("vertexes");
                    for (int index = 0; index + 1 < vertexes.size(); index += 2) {
                        path.add(new RouteCoordinate(
                                vertexes.path(index + 1).asDouble(),
                                vertexes.path(index).asDouble()));
                    }
                }
                for (JsonNode guide : section.path("guides")) {
                    String instruction = guide.path("guidance").asText(guide.path("name").asText("이동"));
                    guides.add(new RouteGuide(
                            instruction,
                            guide.path("y").asDouble(),
                            guide.path("x").asDouble(),
                            guide.path("distance").asInt(),
                            guide.path("duration").asInt()));
                }
            }
        }
        return new DrivingRoute(summary.path("distance").asInt(), summary.path("duration").asInt(),
                summary.path("fare").path("toll").asInt(), summary.path("fare").path("taxi").asInt(),
                List.copyOf(path), List.copyOf(guides));
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    private void requireKey() {
        if (!StringUtils.hasText(apiKey)) throw new IllegalStateException("KAKAO_REST_API_KEY 환경변수가 필요합니다.");
    }

    public record DrivingRoute(int distanceMeters, int durationSeconds, int tollWon, int taxiFareWon,
                               List<RouteCoordinate> path, List<RouteGuide> guides) {
        public DrivingRoute(int distanceMeters, int durationSeconds, int tollWon, int taxiFareWon) {
            this(distanceMeters, durationSeconds, tollWon, taxiFareWon, List.of(), List.of());
        }
    }

    public record RouteCoordinate(double latitude, double longitude) {}

    public record RouteGuide(String instruction, double latitude, double longitude,
                             int distanceMeters, int durationSeconds) {}
}
