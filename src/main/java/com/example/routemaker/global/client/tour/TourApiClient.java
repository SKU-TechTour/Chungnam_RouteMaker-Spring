package com.example.routemaker.global.client.tour;

import com.example.routemaker.global.client.tour.dto.TourOperatingInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourPetInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

@Component
public class TourApiClient {
    private final WebClient client;
    private final String serviceKey;

    public TourApiClient(@Qualifier("tourWebClient") WebClient client,
                         @Value("${external-api.tour.service-key:}") String serviceKey) {
        this.client = client;
        this.serviceKey = serviceKey;
    }

    public List<TourPlaceResponse> searchKeyword(String keyword) {
        JsonNode root = get("/searchKeyword2", builder -> builder
                .queryParam("keyword", keyword).queryParam("numOfRows", 30).queryParam("pageNo", 1));
        List<TourPlaceResponse> result = new ArrayList<>();
        for (JsonNode item : items(root)) {
            result.add(TourPlaceResponse.from(item));
        }
        return result;
    }

    public TourOperatingInfoResponse operatingInfo(String contentId, String contentTypeId) {
        JsonNode root = get("/detailIntro2", builder -> builder
                .queryParam("contentId", contentId).queryParam("contentTypeId", contentTypeId));
        JsonNode item = items(root).stream().findFirst().orElseThrow(
                () -> new IllegalStateException("TourAPI 운영 정보를 찾을 수 없습니다."));
        return TourOperatingInfoResponse.from(contentId, contentTypeId, item);
    }

    public TourPetInfoResponse petInfo(String contentId) {
        JsonNode root = get("/detailPetTour2", builder -> builder.queryParam("contentId", contentId));
        JsonNode item = items(root).stream().findFirst().orElse(null);
        return item == null ? TourPetInfoResponse.empty(contentId) : TourPetInfoResponse.from(contentId, item);
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> params) {
        requireKey();
        return client.get().uri(builder -> params.apply(builder.path(path)
                        .queryParam("serviceKey", serviceKey).queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "ChungnamRouteMaker").queryParam("_type", "json"))
                .build()).retrieve().bodyToMono(JsonNode.class).block(Duration.ofSeconds(10));
    }

    private List<JsonNode> items(JsonNode root) {
        List<JsonNode> result = new ArrayList<>();
        if (root == null) return result;
        JsonNode item = root.path("response").path("body").path("items").path("item");
        if (item.isArray()) item.forEach(result::add);
        else if (item.isObject()) result.add(item);
        return result;
    }

    private void requireKey() {
        if (!StringUtils.hasText(serviceKey)) throw new IllegalStateException("TOUR_API_SERVICE_KEY 환경변수가 필요합니다.");
    }
}
