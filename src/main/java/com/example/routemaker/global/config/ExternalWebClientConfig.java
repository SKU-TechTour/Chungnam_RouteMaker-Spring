package com.example.routemaker.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalWebClientConfig {

    @Bean
    @Qualifier("tourWebClient")
    WebClient tourWebClient(WebClient.Builder builder,
                            @Value("${external-api.tour.base-url:https://apis.data.go.kr/B551011/KorService2}") String baseUrl) {
        return builder.clone().baseUrl(baseUrl).build();
    }

    @Bean
    @Qualifier("weatherWebClient")
    WebClient weatherWebClient(WebClient.Builder builder,
                               @Value("${external-api.weather.base-url:https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}") String baseUrl) {
        return builder.clone().baseUrl(baseUrl).build();
    }
}
