package com.example.routemaker.global.client.tour.dto;

import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

public record TourOperatingInfoResponse(
        String contentId,
        String contentTypeId,
        String restDay,
        String openTime,
        String parking,
        String contact
) {
    public static TourOperatingInfoResponse from(String contentId, String contentTypeId, JsonNode item) {
        return new TourOperatingInfoResponse(contentId, contentTypeId,
                firstText(item, "restdate", "restdateculture", "restdatefood", "restdateshopping"),
                firstText(item, "usetime", "usetimeculture", "opentimefood", "opentime"),
                firstText(item, "parking", "parkingculture", "parkingfood", "parkingshopping"),
                firstText(item, "infocenter", "infocenterculture", "infocenterfood"));
    }

    private static String firstText(JsonNode item, String... names) {
        for (String name : names) {
            if (StringUtils.hasText(item.path(name).asText())) return item.path(name).asText();
        }
        return "정보 없음";
    }
}
