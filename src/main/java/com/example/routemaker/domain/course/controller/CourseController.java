package com.example.routemaker.domain.course.controller;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.service.CourseService;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/recommend")
    public ApiResponse<CourseResponse> recommendCourse(@RequestBody CourseRecommendRequest request) {
        return ApiResponse.success(courseService.recommendCourse(request));
    }

    @GetMapping("/{courseId}/shuffle")
    public ApiResponse<CourseResponse> shufflePlanB(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.shufflePlanB(courseId));
    }
}
