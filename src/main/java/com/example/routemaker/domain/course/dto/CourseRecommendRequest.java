package com.example.routemaker.domain.course.dto;

import com.example.routemaker.global.common.enums.Region;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CourseRecommendRequest {

    private Region region;
    private boolean military;
    private Set<String> concepts = Set.of();
    private int variant;
}
