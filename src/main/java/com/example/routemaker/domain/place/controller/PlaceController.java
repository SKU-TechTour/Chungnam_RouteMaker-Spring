package com.example.routemaker.domain.place.controller;

import com.example.routemaker.domain.place.dto.PlaceFilterRequest;
import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.domain.place.service.PlaceService;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public ApiResponse<List<PlaceResponse>> searchPlaces(@ModelAttribute PlaceFilterRequest request) {
        return ApiResponse.success(placeService.searchPlaces(request));
    }
}
