package com.example.routemaker.domain.place.service;

import com.example.routemaker.domain.place.dto.PlaceFilterRequest;
import com.example.routemaker.global.client.tour.TourApiClient;
import com.example.routemaker.global.client.tour.TourEnrichmentClient;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import com.example.routemaker.global.common.enums.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    private TourApiClient tourApiClient;
    private PlaceService service;

    @BeforeEach
    void setUp() {
        tourApiClient = mock(TourApiClient.class);
        service = new PlaceService(tourApiClient, mock(TourEnrichmentClient.class));
    }

    @Test
    void usesPetApiRegionalListDirectlyWhenPetFilterIsEnabled() {
        PlaceFilterRequest request = new PlaceFilterRequest();
        request.setRegion(Region.GONGJU);
        request.setPetFriendly(true);
        when(tourApiClient.petFriendlyAreaBased("34", "1")).thenReturn(List.of(
                new TourPlaceResponse(
                        "2736857", "12", "A02020700", "메타세콰이어길",
                        "충청남도 공주시 의당면 청룡리", "https://example.com/image.jpg",
                        127.1352143230, 36.4931476363)));

        var result = service.searchPlaces(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("메타세콰이어길");
        assertThat(result.get(0).isPetFriendly()).isTrue();
        verify(tourApiClient).petFriendlyAreaBased("34", "1");
        verify(tourApiClient, never()).areaBased("34", "1");
    }
}
