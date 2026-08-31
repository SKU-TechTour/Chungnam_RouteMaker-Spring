package com.example.routemaker.global.client.tour.dto;

import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

public record TourPetInfoResponse(String contentId, boolean dataAvailable, String detail) {
    public static TourPetInfoResponse empty(String contentId) {
        return new TourPetInfoResponse(contentId, false, "반려동물 동반 정보가 제공되지 않습니다.");
    }

    public static TourPetInfoResponse from(String contentId, JsonNode item) {
        String[] fields = {"etcAcmpyInfo", "acmpyTypeCd", "relaAcdntRiskMtr", "relaPosesFclty"};
        for (String field : fields) {
            String value = item.path(field).asText();
            if (StringUtils.hasText(value)) return new TourPetInfoResponse(contentId, true, value);
        }
        return new TourPetInfoResponse(contentId, true, "세부 정보 없음");
    }
}
