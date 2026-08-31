package com.example.routemaker.global.client.weather;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class WeatherApiClient {
    private final RestClient client;
    private final String serviceKey;

    public WeatherApiClient(@Value("${external-api.weather.base-url:https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}") String baseUrl,
                            @Value("${external-api.weather.service-key:}") String serviceKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    public List<ForecastItem> shortTerm(String baseDate, String baseTime, int nx, int ny) {
        if (!StringUtils.hasText(serviceKey)) throw new IllegalStateException("WEATHER_API_SERVICE_KEY 환경변수가 필요합니다.");
        JsonNode root = client.get().uri(builder -> builder.path("/getVilageFcst")
                        .queryParam("serviceKey", serviceKey).queryParam("pageNo", 1).queryParam("numOfRows", 1000)
                        .queryParam("dataType", "JSON").queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime).queryParam("nx", nx).queryParam("ny", ny).build())
                .retrieve().body(JsonNode.class);
        List<ForecastItem> result = new ArrayList<>();
        if (root == null) return result;
        for (JsonNode item : root.path("response").path("body").path("items").path("item")) {
            result.add(new ForecastItem(item.path("category").asText(), item.path("fcstDate").asText(),
                    item.path("fcstTime").asText(), item.path("fcstValue").asText()));
        }
        return result;
    }

    public boolean isRainy(String region) {
        int[] grid = switch (region) {
            case "GONGJU" -> new int[]{63, 102};
            case "BUYEO" -> new int[]{59, 99};
            default -> new int[]{62, 97};
        };
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(10);
        int[] hours = {2, 5, 8, 11, 14, 17, 20, 23};
        int selected = -1;
        for (int hour : hours) if (hour <= now.getHour()) selected = hour;
        if (selected < 0) {
            now = now.minusDays(1);
            selected = 23;
        }
        String date = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String time = "%02d00".formatted(selected);
        return shortTerm(date, time, grid[0], grid[1]).stream().anyMatch(item ->
                (item.category().equals("PTY") && !item.value().equals("0"))
                        || (item.category().equals("POP") && parseInt(item.value()) >= 60));
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return 0; }
    }

    public record ForecastItem(String category, String forecastDate, String forecastTime, String value) {}
}
