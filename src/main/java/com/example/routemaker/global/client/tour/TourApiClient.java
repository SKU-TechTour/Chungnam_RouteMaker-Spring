package com.example.routemaker.global.client.tour;

import com.example.routemaker.global.client.tour.dto.TourOperatingInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourPetInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.util.retry.Retry;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    /**
     * 한국관광공사 데이터를 저장/캐시하지 않고 요청 시점에 지역기반 API를 호출합니다.
     */
    public List<TourPlaceResponse> areaBased(String areaCode, String sigunguCode) {
        return areaBased(areaCode, sigunguCode, null, 50);
    }

    public List<TourPlaceResponse> areaBased(String areaCode, String sigunguCode,
                                             String contentTypeId, int numOfRows) {
        JsonNode root = get("/areaBasedList2", builder -> {
            builder.queryParam("areaCode", areaCode).queryParam("sigunguCode", sigunguCode)
                    .queryParam("arrange", "A").queryParam("numOfRows", numOfRows).queryParam("pageNo", 1);
            if (StringUtils.hasText(contentTypeId)) {
                builder.queryParam("contentTypeId", contentTypeId);
            }
            return builder;
        });
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

    /** 요청마다 반려동물 동반 가능 contentId 목록을 실시간 조회하며 저장하거나 캐시하지 않습니다. */
    public Set<String> petFriendlyContentIds() {
        JsonNode root = get("/detailPetTour2", builder -> builder
                .queryParam("numOfRows", 10000).queryParam("pageNo", 1));
        return items(root).stream()
                .map(item -> item.path("contentid").asText())
                .filter(StringUtils::hasText)
                .collect(Collectors.toUnmodifiableSet());
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> params) {
        requireKey();
        return client.get().uri(builder -> params.apply(builder.path(path)
                        .queryParam("serviceKey", serviceKey).queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "ChungnamRouteMaker").queryParam("_type", "json"))
                .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(350))
                        .maxBackoff(Duration.ofSeconds(2))
                        .filter(this::isTransientFailure))
                .block(Duration.ofSeconds(30));
    }

    private boolean isTransientFailure(Throwable error) {
        if (error instanceof WebClientResponseException responseError) {
            return responseError.getStatusCode().value() == 429
                    || responseError.getStatusCode().is5xxServerError();
        }
        return true;
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
