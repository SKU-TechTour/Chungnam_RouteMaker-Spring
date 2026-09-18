package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.BookmarkSpotRequest;
import com.example.routemaker.domain.course.dto.CourseBookmarkRequest;
import com.example.routemaker.domain.course.dto.PopularCourseRow;
import com.example.routemaker.domain.course.dto.PopularCourseResponse;
import com.example.routemaker.domain.course.entity.CourseBookmark;
import com.example.routemaker.domain.course.repository.CourseBookmarkRepository;
import com.example.routemaker.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseBookmarkServiceTest {

    private CourseBookmarkRepository repository;
    private CourseBookmarkService service;

    @BeforeEach
    void setUp() {
        repository = mock(CourseBookmarkRepository.class);
        service = new CourseBookmarkService(repository);
    }

    @Test
    void storesOneBookmarkSnapshotPerFirebaseUserAndRoute() {
        User user = User.builder()
                .firebaseUid("firebase-uid")
                .email("tester@example.com")
                .password("firebase")
                .nickname("테스터")
                .build();
        CourseBookmarkRequest request = request();
        when(repository.findByFirebaseUidAndRouteKey("firebase-uid", request.routeKey()))
                .thenReturn(Optional.empty());

        service.save(user, request);

        ArgumentCaptor<CourseBookmark> captor = ArgumentCaptor.forClass(CourseBookmark.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFirebaseUid()).isEqualTo("firebase-uid");
        assertThat(captor.getValue().getRouteKey()).isEqualTo("NONSAN:100--1");
        assertThat(captor.getValue().getSpotsJson()).contains("육군훈련소");
    }

    @Test
    void returnsPopularCoursesWithBookmarkCounts() throws Exception {
        String spots = new ObjectMapper().writeValueAsString(request().spots());
        when(repository.findPopular(any(Pageable.class))).thenReturn(List.of(
                new PopularCourseRow(
                        "NONSAN:100--1", "NONSAN", "입영 코스", spots,
                        12000, 1500, 7L)));

        var result = service.popular(3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).bookmarkCount()).isEqualTo(7L);
        assertThat(result.get(0).popularityScore()).isEqualTo(100D);
        assertThat(result.get(0).spots()).extracting(BookmarkSpotRequest::name)
                .containsExactly("연무식당", "육군훈련소");
    }

    @Test
    void ranksPopularityByBookmarkCountOnly() throws Exception {
        String spots = new ObjectMapper().writeValueAsString(request().spots());
        when(repository.findPopular(any(Pageable.class))).thenReturn(List.of(
                new PopularCourseRow(
                        "NONSAN:popular-bookmark", "NONSAN", "찜 10개", spots,
                        12000, 1500, 10L),
                new PopularCourseRow(
                        "GONGJU:fewer-bookmark", "GONGJU", "찜 5개", spots,
                        12000, 1500, 5L)));

        var result = service.popular(3);

        assertThat(result).extracting(PopularCourseResponse::title)
                .containsExactly("찜 10개", "찜 5개");
        assertThat(result.get(0).rankingBasis()).isEqualTo("코스 찜 수 기준");
    }

    private CourseBookmarkRequest request() {
        return new CourseBookmarkRequest(
                "NONSAN:100--1",
                "NONSAN",
                "입영 코스",
                List.of(
                        new BookmarkSpotRequest(
                                "100", "연무식당", "RESTAURANT", 36.15, 127.12,
                                null, "TOUR_API_REALTIME", "논산", null),
                        new BookmarkSpotRequest(
                                "-1", "육군훈련소", "HERITAGE", 36.1119, 127.1083,
                                null, "SERVICE_ANCHOR", "논산", "13:00 도착")),
                12000,
                1500);
    }
}
