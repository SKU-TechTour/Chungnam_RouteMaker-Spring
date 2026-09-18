package com.example.routemaker.global.client.tour;

import com.example.routemaker.global.client.tour.dto.TourOperatingInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourCommonInfoResponse;
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
import java.util.function.UnaryOperator;

@Component
public class TourApiClient {
    private final WebClient client;
    private final WebClient petClient;
    private final String serviceKey;

    public TourApiClient(@Qualifier("tourWebClient") WebClient client,
                         @Qualifier("petTourWebClient") WebClient petClient,
                         @Value("${external-api.tour.service-key:}") String serviceKey) {
        this.client = client;
        this.petClient = petClient;
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

    public TourCommonInfoResponse commonInfo(String contentId) {
        JsonNode root = get("/detailCommon2", builder -> builder
                // KorService2의 현재 detailCommon2는 구형 *YN 파라미터를 받지 않고
                // contentId만으로 title/address/image/overview 전체를 반환합니다.
                .queryParam("contentId", contentId));
        JsonNode item = items(root).stream().findFirst().orElseThrow(
                () -> new IllegalStateException("TourAPI 소개 정보를 찾을 수 없습니다."));
        return TourCommonInfoResponse.from(contentId, item);
    }

    public TourPetInfoResponse petInfo(String contentId) {
        JsonNode root = get(petClient, "/detailPetTour2",
                builder -> builder.queryParam("contentId", contentId));
        JsonNode item = items(root).stream().findFirst().orElse(null);
        return item == null ? TourPetInfoResponse.empty(contentId) : TourPetInfoResponse.from(contentId, item);
    }

    /**
     * 반려동물 동반 여행 API를 지역별 원천 목록으로 직접 조회합니다.
     * 일반 관광정보 목록과 교집합을 만들면 목록 페이지 제한 때문에 공식 장소가
     * 누락될 수 있으므로, 반려동물 필터에서는 이 결과를 그대로 사용합니다.
     */
    public List<TourPlaceResponse> petFriendlyAreaBased(String areaCode, String sigunguCode) {
        JsonNode root = get(petClient, "/areaBasedList2", builder -> builder
                .queryParam("areaCode", areaCode)
                .queryParam("sigunguCode", sigunguCode)
                .queryParam("arrange", "A")
                .queryParam("numOfRows", 100)
                .queryParam("pageNo", 1));
        List<TourPlaceResponse> result = new ArrayList<>();
        for (JsonNode item : items(root)) {
            result.add(TourPlaceResponse.from(item));
        }
        return result;
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> params) {
        return get(client, path, params);
    }

    private JsonNode get(WebClient webClient, String path, UnaryOperator<UriBuilder> params) {
        requireKey();
        return webClient.get().uri(builder -> params.apply(builder.path(path)
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
