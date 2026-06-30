package com.example.routemaker.domain.course.repository;

import com.example.routemaker.domain.course.entity.Course;
import com.example.routemaker.domain.course.entity.CourseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseItemRepository extends JpaRepository<CourseItem, Long> {

    List<CourseItem> findByCourseOrderByStepOrderAsc(Course course);
}
