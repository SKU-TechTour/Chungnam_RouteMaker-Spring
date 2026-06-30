package com.example.routemaker.domain.course.dto;

import com.example.routemaker.domain.course.entity.Course;
import com.example.routemaker.domain.course.entity.CourseItem;
import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.global.common.enums.Region;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private Region region;
    private boolean indoor;
    private List<PlaceResponse> combo;

    public static CourseResponse from(Course course, List<CourseItem> items) {
        List<PlaceResponse> combo = items.stream()
                .map(item -> PlaceResponse.from(item.getPlace()))
                .toList();

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .region(course.getRegion())
                .indoor(course.isIndoor())
                .combo(combo)
                .build();
    }
}
