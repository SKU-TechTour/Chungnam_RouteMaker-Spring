package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.BookmarkSpotRequest;
import com.example.routemaker.domain.course.dto.CourseBookmarkRequest;
import com.example.routemaker.domain.course.dto.PopularCourseResponse;
import com.example.routemaker.domain.course.entity.CourseBookmark;
import com.example.routemaker.domain.course.repository.CourseBookmarkRepository;
import com.example.routemaker.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CourseBookmarkService {

    private static final TypeReference<List<BookmarkSpotRequest>> SPOT_LIST = new TypeReference<>() {};

    private final CourseBookmarkRepository bookmarkRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void save(User user, CourseBookmarkRequest request) {
        String uid = requireFirebaseUid(user);
        String spotsJson = writeSpots(request.spots());
        CourseBookmark bookmark = bookmarkRepository
                .findByFirebaseUidAndRouteKey(uid, request.routeKey())
                .orElseGet(() -> CourseBookmark.builder()
                        .firebaseUid(uid)
                        .routeKey(request.routeKey())
                        .region(request.region())
                        .title(request.title())
                        .spotsJson(spotsJson)
                        .totalDistanceMeters(Math.max(0, request.totalDistanceMeters()))
                        .totalDurationSeconds(Math.max(0, request.totalDurationSeconds()))
                        .build());
        bookmark.updateSnapshot(
                request.region(), request.title(), spotsJson,
                Math.max(0, request.totalDistanceMeters()),
                Math.max(0, request.totalDurationSeconds()));
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void remove(User user, String routeKey) {
        bookmarkRepository.deleteByFirebaseUidAndRouteKey(requireFirebaseUid(user), routeKey);
    }

    @Transactional(readOnly = true)
    public List<PopularCourseResponse> popular(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 10));
        var candidates = bookmarkRepository.findPopular(PageRequest.of(0, 50));
        if (candidates.isEmpty()) return List.of();

        double maxBookmarks = candidates.stream().mapToLong(row -> row.bookmarkCount()).max().orElse(1);

        return candidates.stream()
                .map(row -> {
                    double bookmarkScore = maxBookmarks <= 0 ? 0 : row.bookmarkCount() / maxBookmarks;
                    double score = Math.round(bookmarkScore * 1000D) / 10D;
                    return new PopularCourseResponse(
                            row.routeKey(), row.region(), row.title(), readSpots(row.spotsJson()),
                            row.totalDistanceMeters(), row.totalDurationSeconds(), row.bookmarkCount(),
                            score, "코스 찜 수 기준");
                })
                .sorted(Comparator.comparingDouble(PopularCourseResponse::popularityScore).reversed()
                        .thenComparing(Comparator.comparingLong(PopularCourseResponse::bookmarkCount).reversed()))
                .limit(limit)
                .toList();
    }

    private String requireFirebaseUid(User user) {
        if (user == null || user.getFirebaseUid() == null || user.getFirebaseUid().isBlank()) {
            throw new IllegalArgumentException("Firebase 사용자가 필요합니다.");
        }
        return user.getFirebaseUid();
    }

    private String writeSpots(List<BookmarkSpotRequest> spots) {
        try {
            return objectMapper.writeValueAsString(spots);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("코스 장소 정보를 저장할 수 없습니다.", exception);
        }
    }

    private List<BookmarkSpotRequest> readSpots(String json) {
        try {
            return objectMapper.readValue(json, SPOT_LIST);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 코스 장소 정보를 읽을 수 없습니다.", exception);
        }
    }
}
