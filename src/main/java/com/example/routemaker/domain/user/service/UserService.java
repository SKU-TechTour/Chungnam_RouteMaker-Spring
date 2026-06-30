package com.example.routemaker.domain.user.service;

import com.example.routemaker.domain.user.dto.MyPageResponse;
import com.example.routemaker.domain.user.dto.UserUpdateRequest;
import com.example.routemaker.domain.user.entity.TravelRecord;
import com.example.routemaker.domain.user.entity.User;
import com.example.routemaker.domain.user.repository.TravelRecordRepository;
import com.example.routemaker.domain.user.repository.UserRepository;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TravelRecordRepository travelRecordRepository;

    public MyPageResponse getMyPage(String email) {
        User user = findUserByEmail(email);
        List<MyPageResponse.TravelRecordSummary> records = travelRecordRepository
                .findByUserOrderByCompletedAtDesc(user)
                .stream()
                .map(this::toSummary)
                .toList();

        return MyPageResponse.from(user, records);
    }

    @Transactional
    public MyPageResponse updateProfile(String email, UserUpdateRequest request) {
        User user = findUserByEmail(email);
        // TODO: User 엔티티에 update 메서드 추가 후 반영
        return getMyPage(email);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private MyPageResponse.TravelRecordSummary toSummary(TravelRecord record) {
        return MyPageResponse.TravelRecordSummary.builder()
                .id(record.getId())
                .region(record.getRegion().name())
                .courseTitle(record.getCourseTitle())
                .completedAt(record.getCompletedAt().toString())
                .build();
    }
}
