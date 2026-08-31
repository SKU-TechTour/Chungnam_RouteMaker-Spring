package com.example.routemaker.global.client.tour;

import com.example.routemaker.global.client.tour.dto.TourOperatingInfoResponse;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class TourApiDtoTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesTourSearchItem() throws Exception {
        JsonNode item = objectMapper.readTree("""
                {"contentid":"126508","contenttypeid":"12","title":"공산성",
                 "addr1":"충남 공주시 웅진로 280","firstimage":"https://image.example/gongsanseong.jpg",
                 "mapx":"127.1264411","mapy":"36.4631426"}
                """);

        TourPlaceResponse response = TourPlaceResponse.from(item);

        assertThat(response.name()).isEqualTo("공산성");
        assertThat(response.contentTypeId()).isEqualTo("12");
        assertThat(response.longitude()).isEqualTo(127.1264411);
    }

    @Test
    void normalizesRestaurantOperatingFields() throws Exception {
        JsonNode item = objectMapper.readTree("""
                {"restdatefood":"매주 월요일","opentimefood":"11:00~20:00",
                 "parkingfood":"주차 가능","infocenterfood":"041-000-0000"}
                """);

        TourOperatingInfoResponse response = TourOperatingInfoResponse.from("1", "39", item);

        assertThat(response.restDay()).isEqualTo("매주 월요일");
        assertThat(response.openTime()).isEqualTo("11:00~20:00");
    }
}
