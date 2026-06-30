package com.example.routemaker.domain.course.dto;

import com.example.routemaker.global.common.enums.Region;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseRecommendRequest {

    private Region region;
    private boolean military;
}
