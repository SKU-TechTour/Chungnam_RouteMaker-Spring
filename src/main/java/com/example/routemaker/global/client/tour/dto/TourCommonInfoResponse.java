package com.example.routemaker.global.client.tour.dto;

import tools.jackson.databind.JsonNode;

public record TourCommonInfoResponse(
        String contentId,
        String title,
        String address,
        String imageUrl,
        String overview,
        String homepage,
        String telephone
) {
    public static TourCommonInfoResponse from(String contentId, JsonNode item) {
        return new TourCommonInfoResponse(
                contentId,
                item.path("title").asText(),
                item.path("addr1").asText(),
                item.path("firstimage").asText(),
                item.path("overview").asText(),
                item.path("homepage").asText(),
                item.path("tel").asText()
        );
    }
}
