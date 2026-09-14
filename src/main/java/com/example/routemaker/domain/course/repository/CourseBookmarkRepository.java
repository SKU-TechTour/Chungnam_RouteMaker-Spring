package com.example.routemaker.domain.course.repository;

import com.example.routemaker.domain.course.dto.PopularCourseRow;
import com.example.routemaker.domain.course.entity.CourseBookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseBookmarkRepository extends JpaRepository<CourseBookmark, Long> {

    Optional<CourseBookmark> findByFirebaseUidAndRouteKey(String firebaseUid, String routeKey);

    void deleteByFirebaseUidAndRouteKey(String firebaseUid, String routeKey);

    @Query("""
            select new com.example.routemaker.domain.course.dto.PopularCourseRow(
                b.routeKey, b.region, max(b.title), max(b.spotsJson),
                max(b.totalDistanceMeters), max(b.totalDurationSeconds), count(b)
            )
            from CourseBookmark b
            group by b.routeKey, b.region
            order by count(b) desc, max(b.id) desc
            """)
    List<PopularCourseRow> findPopular(Pageable pageable);
}
