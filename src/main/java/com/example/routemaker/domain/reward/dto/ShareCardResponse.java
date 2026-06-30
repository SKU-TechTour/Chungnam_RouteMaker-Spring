package com.example.routemaker.domain.reward.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareCardResponse {

    private String shareUrl;
    private String title;
    private String imageUrl;
}
