package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.entity.Course;
import com.example.routemaker.domain.course.entity.CourseItem;
import com.example.routemaker.domain.course.repository.CourseItemRepository;
import com.example.routemaker.domain.course.repository.CourseRepository;
import com.example.routemaker.global.client.weather.WeatherApiClient;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;
    private final WeatherApiClient weatherApiClient;

    public CourseResponse recommendCourse(CourseRecommendRequest request) {
        boolean rainy = weatherApiClient.isRainy(request.getRegion().name());
        boolean indoor = rainy;

        List<Course> courses = courseRepository.findByRegionAndIndoor(request.getRegion(), indoor);
        if (courses.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "추천 가능한 코스가 없습니다.");
        }

        Course course = courses.get(0);
        List<CourseItem> items = courseItemRepository.findByCourseOrderByStepOrderAsc(course);
        return CourseResponse.from(course, items);
    }

    public CourseResponse shufflePlanB(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "코스를 찾을 수 없습니다."));

        List<CourseItem> items = courseItemRepository.findByCourseOrderByStepOrderAsc(course);
        List<CourseItem> shuffled = new java.util.ArrayList<>(items);
        Collections.shuffle(shuffled);

        return CourseResponse.from(course, shuffled);
    }
}
