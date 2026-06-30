package com.example.routemaker.domain.course.repository;

import com.example.routemaker.domain.course.entity.Course;
import com.example.routemaker.global.common.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByRegionAndIndoor(Region region, boolean indoor);
}
