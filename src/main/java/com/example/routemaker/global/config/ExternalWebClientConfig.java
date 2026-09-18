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
        return builder.clone()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    @Bean
    @Qualifier("petTourWebClient")
    WebClient petTourWebClient(
            WebClient.Builder builder,
            @Value("${external-api.pet.base-url:https://apis.data.go.kr/B551011/KorPetTourService2}") String baseUrl
    ) {
        return builder.clone()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    @Bean
    @Qualifier("withTourWebClient")
    WebClient withTourWebClient(WebClient.Builder builder,
                                @Value("${external-api.with-tour.base-url:https://apis.data.go.kr/B551011/KorWithService2}") String baseUrl) {
        return externalClient(builder, baseUrl);
    }

    @Bean
    @Qualifier("congestionWebClient")
    WebClient congestionWebClient(WebClient.Builder builder,
                                   @Value("${external-api.congestion.base-url:https://apis.data.go.kr/B551011/TatsCnctrRateService}") String baseUrl) {
        return externalClient(builder, baseUrl);
    }

    @Bean
    @Qualifier("audioGuideWebClient")
    WebClient audioGuideWebClient(WebClient.Builder builder,
                                  @Value("${external-api.audio.base-url:https://apis.data.go.kr/B551011/Odii}") String baseUrl) {
        return externalClient(builder, baseUrl);
    }

    @Bean
    @Qualifier("weatherWebClient")
    WebClient weatherWebClient(WebClient.Builder builder,
                               @Value("${external-api.weather.base-url:https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}") String baseUrl) {
        return builder.clone().baseUrl(baseUrl).build();
    }

    private WebClient externalClient(WebClient.Builder builder, String baseUrl) {
        return builder.clone()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
}
