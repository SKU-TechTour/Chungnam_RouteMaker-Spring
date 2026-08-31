package com.example.routemaker.global.client.weather;

import com.example.routemaker.global.client.weather.dto.WeatherForecastItemResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherApiDtoTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesVillageForecastItem() throws Exception {
        JsonNode item = objectMapper.readTree("""
                {"category":"POP","fcstDate":"20260901","fcstTime":"1200","fcstValue":"60"}
                """);

        WeatherForecastItemResponse response = WeatherForecastItemResponse.from(item);

        assertThat(response.category()).isEqualTo("POP");
        assertThat(response.value()).isEqualTo("60");
    }
}
