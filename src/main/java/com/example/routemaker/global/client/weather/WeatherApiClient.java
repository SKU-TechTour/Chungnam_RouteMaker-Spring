package com.example.routemaker.global.client.weather;

import com.example.routemaker.domain.course.dto.HourlyWeatherResponse;
import com.example.routemaker.global.client.weather.dto.WeatherForecastItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WeatherApiClient {
    private final WebClient client;
    private final String serviceKey;

    public WeatherApiClient(@Qualifier("weatherWebClient") WebClient client,
                            @Value("${external-api.weather.service-key:}") String serviceKey) {
        this.client = client;
        this.serviceKey = serviceKey;
    }

    public List<WeatherForecastItemResponse> shortTerm(String baseDate, String baseTime, int nx, int ny) {
        if (!StringUtils.hasText(serviceKey)) throw new IllegalStateException("WEATHER_API_SERVICE_KEY 환경변수가 필요합니다.");
        JsonNode root = client.get().uri(builder -> builder.path("/getVilageFcst")
                        .queryParam("serviceKey", serviceKey).queryParam("pageNo", 1).queryParam("numOfRows", 1000)
                        .queryParam("dataType", "JSON").queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime).queryParam("nx", nx).queryParam("ny", ny).build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(12))
                .doOnError(error -> log.warn(
                        "Weather API request failed baseDate={}, baseTime={}: {}",
                        baseDate, baseTime, error.toString()))
                .block(Duration.ofSeconds(13));
        List<WeatherForecastItemResponse> result = new ArrayList<>();
        if (root == null) return result;
        for (JsonNode item : root.path("response").path("body").path("items").path("item")) {
            result.add(WeatherForecastItemResponse.from(item));
        }
        return result;
    }

    public boolean isRainy(String region) {
        return hourly(region).stream().anyMatch(HourlyWeatherResponse::precipitationExpected);
    }

    public List<HourlyWeatherResponse> hourly(String region) {
        int[] grid = switch (region) {
            case "GONGJU" -> new int[]{63, 102};
            case "BUYEO" -> new int[]{59, 99};
            default -> new int[]{62, 97};
        };
        LocalDateTime currentHour = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = currentHour.minusMinutes(10);
        int[] hours = {2, 5, 8, 11, 14, 17, 20, 23};
        int selected = -1;
        for (int hour : hours) if (hour <= now.getHour()) selected = hour;
        if (selected < 0) {
            now = now.minusDays(1);
            selected = 23;
        }
        String date = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String time = "%02d00".formatted(selected);
        List<WeatherForecastItemResponse> raw = shortTerm(date, time, grid[0], grid[1]);
        Map<String, MutableHourlyWeather> grouped = new LinkedHashMap<>();
        for (WeatherForecastItemResponse item : raw) {
            String key = item.forecastDate() + item.forecastTime();
            MutableHourlyWeather weather = grouped.computeIfAbsent(
                    key, ignored -> new MutableHourlyWeather(
                            item.forecastTime(),
                            LocalDateTime.parse(key, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))));
            switch (item.category()) {
                case "TMP" -> weather.temperature = parseInt(item.value());
                case "POP" -> weather.precipitationProbability = parseInt(item.value());
                case "PTY" -> weather.precipitationType = parseInt(item.value());
                default -> { }
            }
        }
        return grouped.values().stream()
                .filter(item -> item.temperature != null && !item.forecastAt.isBefore(currentHour))
                .limit(8)
                .map(item -> new HourlyWeatherResponse(
                        item.time.substring(0, 2) + ":00",
                        item.temperature,
                        item.precipitationProbability,
                        item.precipitationType > 0 || item.precipitationProbability >= 60))
                .toList();
    }

    private static final class MutableHourlyWeather {
        private final String time;
        private final LocalDateTime forecastAt;
        private Integer temperature;
        private int precipitationProbability;
        private int precipitationType;

        private MutableHourlyWeather(String time, LocalDateTime forecastAt) {
            this.time = time;
            this.forecastAt = forecastAt;
        }
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return 0; }
    }
}
