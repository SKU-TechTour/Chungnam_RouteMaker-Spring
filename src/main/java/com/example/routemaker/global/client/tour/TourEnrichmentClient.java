package com.example.routemaker.global.client.tour;

import com.example.routemaker.global.common.enums.Region;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TourEnrichmentClient {

    private static final Duration DETAIL_TTL = Duration.ofMinutes(15);
    private final WebClient withTourClient;
    private final WebClient congestionClient;
    private final WebClient audioClient;
    private final String serviceKey;
    private final Map<String, MapCacheEntry> detailCache = new ConcurrentHashMap<>();

    public TourEnrichmentClient(
            @Qualifier("withTourWebClient") WebClient withTourClient,
            @Qualifier("congestionWebClient") WebClient congestionClient,
            @Qualifier("audioGuideWebClient") WebClient audioClient,
            @Value("${external-api.tour.service-key:}") String serviceKey) {
        this.withTourClient = withTourClient;
        this.congestionClient = congestionClient;
        this.audioClient = audioClient;
        this.serviceKey = serviceKey;
    }

    public Set<String> accessibleContentIds(Region region) {
        try {
            JsonNode root;
            try {
                root = get(withTourClient, "/areaBasedList2", builder -> areaParams(builder, region));
            } catch (RuntimeException firstFailure) {
                root = get(withTourClient, "/areaBasedSyncList2", builder -> areaParams(builder, region));
            }
            return items(root).stream()
                    .map(item -> text(item, "contentid"))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (RuntimeException error) {
            log.warn("KorWithService2 list failed region={}: {}", region, error.getMessage());
            return Set.of();
        }
    }

    public Map<String, Object> accessibility(String contentId) {
        return cached("accessibility:" + contentId, () -> {
            try {
                JsonNode root = get(withTourClient, "/detailWithTour2",
                        builder -> builder.queryParam("contentId", contentId));
                JsonNode item = items(root).stream().findFirst().orElse(null);
                if (item == null) return unavailable("이동 편의 정보가 제공되지 않는 장소입니다.");
                List<Map<String, Object>> features = List.of(
                        feature("장애인 주차구역", text(item, "parking")),
                        feature("휠체어", text(item, "wheelchair")),
                        feature("접근로·경사로", join(text(item, "route"), text(item, "exit"))),
                        feature("엘리베이터", text(item, "elevator")),
                        feature("장애인 화장실", text(item, "restroom")),
                        feature("유모차", text(item, "stroller")),
                        feature("수유·영유아 시설", join(text(item, "lactationroom"), text(item, "babysparechair")))
                );
                boolean available = features.stream().anyMatch(value -> Boolean.TRUE.equals(value.get("provided")));
                Map<String, Object> result = new HashMap<>();
                result.put("available", available);
                result.put("contentId", contentId);
                result.put("features", features);
                result.put("source", "한국관광공사 무장애 여행 정보");
                return Map.copyOf(result);
            } catch (RuntimeException error) {
                log.warn("KorWithService2 detail failed contentId={}: {}", contentId, error.getMessage());
                return unavailable("이동 편의 정보를 불러오지 못했습니다.");
            }
        });
    }

    public Map<String, Object> congestion(Region region, String attractionName) {
        return cached("congestion:" + region + ':' + attractionName, () -> {
            try {
                JsonNode root = get(congestionClient, "/tatsCnctrRatedList", builder -> builder
                        .queryParam("numOfRows", 30)
                        .queryParam("pageNo", 1)
                        .queryParam("areaCd", "44")
                        .queryParam("signguCd", administrativeSigunguCode(region))
                        .queryParam("tAtsNm", attractionName));
                JsonNode item = items(root).stream()
                        .max(Comparator.comparing(value -> text(value, "baseYmd")))
                        .orElse(null);
                if (item == null) return unavailable("예상 혼잡도 정보가 없습니다.");
                double rate = item.path("cnctrRate").asDouble(-1);
                if (rate < 0) return unavailable("예상 혼잡도 정보가 없습니다.");
                String level = rate <= 40 ? "여유" : rate <= 70 ? "보통" : "혼잡 예상";
                Map<String, Object> result = new HashMap<>();
                result.put("available", true);
                result.put("attractionName", text(item, "tAtsNm"));
                result.put("baseDate", text(item, "baseYmd"));
                result.put("rate", rate);
                result.put("level", level);
                result.put("notice", "이동통신 데이터를 기반으로 한 향후 방문 집중률 예측값입니다.");
                result.put("source", "한국관광공사 관광지 집중률 방문자 추이 예측 정보");
                return Map.copyOf(result);
            } catch (RuntimeException error) {
                log.warn("congestion failed region={}, attraction={}: {}", region, attractionName, error.getMessage());
                return unavailable("예상 혼잡도를 불러오지 못했습니다.");
            }
        });
    }

    public Map<String, Object> audioGuide(String attractionName) {
        return cached("audio:" + attractionName, () -> {
            try {
                JsonNode themeRoot = get(audioClient, "/themeSearchList", builder -> builder
                        .queryParam("numOfRows", 10).queryParam("pageNo", 1)
                        .queryParam("keyword", attractionName).queryParam("langCode", "ko"));
                JsonNode theme = items(themeRoot).stream()
                        .min(Comparator.comparingInt(item -> nameDistance(attractionName, text(item, "title"))))
                        .orElse(null);
                if (theme == null) return unavailable("제공되는 오디오 해설이 없습니다.");
                String tid = text(theme, "tid");
                String tlid = text(theme, "tlid");
                JsonNode storyRoot = get(audioClient, "/storyBasedList", builder -> builder
                        .queryParam("numOfRows", 20).queryParam("pageNo", 1)
                        .queryParam("langCode", "ko").queryParam("tid", tid).queryParam("tlid", tlid));
                JsonNode story = items(storyRoot).stream()
                        .filter(item -> StringUtils.hasText(text(item, "audioUrl")))
                        .findFirst().orElse(null);
                if (story == null) return unavailable("제공되는 오디오 해설이 없습니다.");
                Map<String, Object> result = new HashMap<>();
                result.put("available", true);
                result.put("title", text(story, "audioTitle"));
                result.put("script", text(story, "script"));
                result.put("audioUrl", secureUrl(text(story, "audioUrl")));
                result.put("imageUrl", secureUrl(text(story, "imageUrl")));
                result.put("source", "한국관광공사 오디 오디오 가이드");
                return Map.copyOf(result);
            } catch (RuntimeException error) {
                log.warn("Odii failed attraction={}: {}", attractionName, error.getMessage());
                return unavailable("오디오 해설을 불러오지 못했습니다.");
            }
        });
    }

    private UriBuilder areaParams(UriBuilder builder, Region region) {
        return builder.queryParam("areaCode", "34")
                .queryParam("sigunguCode", tourSigunguCode(region))
                .queryParam("numOfRows", 500).queryParam("pageNo", 1);
    }

    private String tourSigunguCode(Region region) {
        return switch (region) {
            case GONGJU -> "1";
            case NONSAN -> "3";
            case BUYEO -> "6";
        };
    }

    private String administrativeSigunguCode(Region region) {
        return switch (region) {
            case GONGJU -> "44150";
            case NONSAN -> "44230";
            case BUYEO -> "44760";
        };
    }

    private JsonNode get(WebClient client, String path, UnaryOperator<UriBuilder> params) {
        requireKey();
        return client.get().uri(builder -> params.apply(builder.path(path)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "ChungnamRouteMaker")
                        .queryParam("_type", "json")).build())
                .retrieve().bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(8))
                .retryWhen(Retry.backoff(1, Duration.ofMillis(300)).filter(this::isTransientFailure))
                .block(Duration.ofSeconds(18));
    }

    private boolean isTransientFailure(Throwable error) {
        if (error instanceof WebClientResponseException responseError) {
            return responseError.getStatusCode().value() == 429 || responseError.getStatusCode().is5xxServerError();
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

    private String text(JsonNode node, String field) {
        return node == null ? "" : node.path(field).asText("").trim();
    }

    private Map<String, Object> feature(String label, String value) {
        boolean provided = StringUtils.hasText(value);
        return Map.of("label", label, "value", provided ? value : "정보 없음", "provided", provided);
    }

    private String join(String first, String second) {
        if (!StringUtils.hasText(first)) return second;
        if (!StringUtils.hasText(second)) return first;
        return first + " · " + second;
    }

    private String secureUrl(String value) {
        return value.startsWith("http://") ? "https://" + value.substring(7) : value;
    }

    private Map<String, Object> unavailable(String message) {
        return Map.of("available", false, "message", message);
    }

    private int nameDistance(String requested, String candidate) {
        String left = requested.replaceAll("\\s", "");
        String right = candidate.replaceAll("\\s", "");
        if (left.equals(right)) return 0;
        if (right.contains(left) || left.contains(right)) return 1;
        return 10 + Math.abs(left.length() - right.length());
    }

    private Map<String, Object> cached(String key, Supplier<Map<String, Object>> supplier) {
        MapCacheEntry existing = detailCache.get(key);
        if (existing != null && !existing.expired()) return existing.value();
        Map<String, Object> value = supplier.get();
        detailCache.put(key, new MapCacheEntry(value, System.nanoTime() + DETAIL_TTL.toNanos()));
        return value;
    }

    private void requireKey() {
        if (!StringUtils.hasText(serviceKey)) {
            throw new IllegalStateException("TOUR_API_SERVICE_KEY 환경변수가 필요합니다.");
        }
    }

    private record MapCacheEntry(Map<String, Object> value, long expiresAtNanos) {
        boolean expired() { return System.nanoTime() >= expiresAtNanos; }
    }

}
