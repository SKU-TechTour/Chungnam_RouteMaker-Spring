package com.example.routemaker.domain.course.controller;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.dto.CourseBookmarkRequest;
import com.example.routemaker.domain.course.dto.PopularCourseResponse;
import com.example.routemaker.domain.course.dto.RoutePreviewRequest;
import com.example.routemaker.domain.course.dto.RoutePreviewResponse;
import com.example.routemaker.domain.course.service.CourseService;
import com.example.routemaker.domain.course.service.CourseBookmarkService;
import com.example.routemaker.domain.user.entity.User;
import com.example.routemaker.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseBookmarkService courseBookmarkService;

    @PostMapping("/recommend")
    public ApiResponse<CourseResponse> recommendCourse(@RequestBody CourseRecommendRequest request) {
        return ApiResponse.success(courseService.recommendCourse(request));
    }

    @PostMapping("/recommendations")
    public ApiResponse<List<CourseResponse>> recommendCourses(
            @RequestBody CourseRecommendRequest request) {
        return ApiResponse.success(courseService.recommendCourses(request));
    }

    @PostMapping("/route-preview")
    public ApiResponse<RoutePreviewResponse> previewRoute(@RequestBody RoutePreviewRequest request) {
        return ApiResponse.success(courseService.previewRoute(request));
    }

    @PostMapping("/bookmarks")
    public ApiResponse<Void> saveBookmark(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CourseBookmarkRequest request) {
        courseBookmarkService.save(user, request);
        return ApiResponse.success("찜한 코스를 저장했습니다.", null);
    }

    @DeleteMapping("/bookmarks/{routeKey}")
    public ApiResponse<Void> removeBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable String routeKey) {
        courseBookmarkService.remove(user, routeKey);
        return ApiResponse.success("찜한 코스를 삭제했습니다.", null);
    }

    @GetMapping("/popular")
    public ApiResponse<List<PopularCourseResponse>> popularCourses(
            @RequestParam(defaultValue = "3") int limit) {
        return ApiResponse.success(courseBookmarkService.popular(limit));
    }

    @GetMapping("/{courseId}/shuffle")
    public ApiResponse<CourseResponse> shufflePlanB(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.shufflePlanB(courseId));
    }
}
