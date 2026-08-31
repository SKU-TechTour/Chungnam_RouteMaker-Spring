package com.example.routemaker.global.client.tour;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class TourApiClient {
    private final RestClient client;
    private final String serviceKey;

    public TourApiClient(@Value("${external-api.tour.base-url:https://apis.data.go.kr/B551011/KorService2}") String baseUrl,
                         @Value("${external-api.tour.service-key:}") String serviceKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    public List<TourPlace> searchKeyword(String keyword) {
        JsonNode root = get("/searchKeyword2", builder -> builder
                .queryParam("keyword", keyword).queryParam("numOfRows", 30).queryParam("pageNo", 1));
        List<TourPlace> result = new ArrayList<>();
        for (JsonNode item : items(root)) {
            result.add(new TourPlace(item.path("contentid").asText(), item.path("contenttypeid").asText(),
                    item.path("title").asText(), item.path("addr1").asText(), item.path("firstimage").asText(),
                    item.path("mapx").asDouble(), item.path("mapy").asDouble()));
        }
        return result;
    }

    public OperatingInfo operatingInfo(String contentId, String contentTypeId) {
        JsonNode root = get("/detailIntro2", builder -> builder
                .queryParam("contentId", contentId).queryParam("contentTypeId", contentTypeId));
        JsonNode item = items(root).stream().findFirst().orElseThrow(
                () -> new IllegalStateException("TourAPI 운영 정보를 찾을 수 없습니다."));
        return new OperatingInfo(contentId, contentTypeId,
                firstText(item, "restdate", "restdateculture", "restdatefood", "restdateshopping"),
                firstText(item, "usetime", "usetimeculture", "opentimefood", "opentime"),
                firstText(item, "parking", "parkingculture", "parkingfood", "parkingshopping"),
                firstText(item, "infocenter", "infocenterculture", "infocenterfood"));
    }

    public PetInfo petInfo(String contentId) {
        JsonNode root = get("/detailPetTour2", builder -> builder.queryParam("contentId", contentId));
        JsonNode item = items(root).stream().findFirst().orElse(null);
        if (item == null) return new PetInfo(contentId, false, "반려동물 동반 정보가 제공되지 않습니다.");
        return new PetInfo(contentId, true,
                firstText(item, "acmpyTypeCd", "etcAcmpyInfo", "relaAcdntRiskMtr", "relaPosesFclty"));
    }

    private JsonNode get(String path, java.util.function.UnaryOperator<org.springframework.web.util.UriBuilder> params) {
        requireKey();
        return client.get().uri(builder -> params.apply(builder.path(path)
                        .queryParam("serviceKey", serviceKey).queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "ChungnamRouteMaker").queryParam("_type", "json"))
                .build()).retrieve().body(JsonNode.class);
    }

    private List<JsonNode> items(JsonNode root) {
        List<JsonNode> result = new ArrayList<>();
        if (root == null) return result;
        JsonNode item = root.path("response").path("body").path("items").path("item");
        if (item.isArray()) item.forEach(result::add);
        else if (item.isObject()) result.add(item);
        return result;
    }

    private String firstText(JsonNode item, String... names) {
        for (String name : names) if (StringUtils.hasText(item.path(name).asText())) return item.path(name).asText();
        return "정보 없음";
    }

    private void requireKey() {
        if (!StringUtils.hasText(serviceKey)) throw new IllegalStateException("TOUR_API_SERVICE_KEY 환경변수가 필요합니다.");
    }

    public record TourPlace(String contentId, String contentTypeId, String name, String address,
                            String imageUrl, double longitude, double latitude) {}
    public record OperatingInfo(String contentId, String contentTypeId, String restDay,
                                String openTime, String parking, String contact) {}
    public record PetInfo(String contentId, boolean dataAvailable, String detail) {}
}
