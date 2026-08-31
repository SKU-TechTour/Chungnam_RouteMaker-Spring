package com.example.routemaker.global.client.kakao;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class KakaoLocalApiClient {
    private final RestClient client;
    private final String apiKey;

    public KakaoLocalApiClient(@Value("${external-api.kakao.local-base-url:https://dapi.kakao.com}") String baseUrl,
                               @Value("${external-api.kakao.rest-api-key:}") String apiKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public List<KakaoPlace> searchKeyword(String query, String categoryGroupCode) {
        requireKey();
        JsonNode root = client.get().uri(builder -> {
                    builder.path("/v2/local/search/keyword.json").queryParam("query", query);
                    if (StringUtils.hasText(categoryGroupCode)) builder.queryParam("category_group_code", categoryGroupCode);
                    return builder.build();
                }).header("Authorization", "KakaoAK " + apiKey).retrieve().body(JsonNode.class);
        List<KakaoPlace> result = new ArrayList<>();
        if (root == null) return result;
        for (JsonNode item : root.path("documents")) {
            result.add(new KakaoPlace(item.path("id").asText(), item.path("place_name").asText(),
                    item.path("category_group_code").asText(), item.path("address_name").asText(),
                    item.path("road_address_name").asText(), item.path("place_url").asText(),
                    item.path("x").asDouble(), item.path("y").asDouble()));
        }
        return result;
    }

    private void requireKey() {
        if (!StringUtils.hasText(apiKey)) throw new IllegalStateException("KAKAO_REST_API_KEY 환경변수가 필요합니다.");
    }

    public record KakaoPlace(String id, String name, String categoryGroupCode, String address,
                             String roadAddress, String placeUrl, double longitude, double latitude) {}
}
