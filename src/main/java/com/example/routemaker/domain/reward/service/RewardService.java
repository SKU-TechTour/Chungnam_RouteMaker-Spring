package com.example.routemaker.domain.reward.service;

import com.example.routemaker.domain.reward.client.ChakApiClient;
import com.example.routemaker.domain.reward.dto.ShareCardResponse;
import com.example.routemaker.domain.reward.dto.StampResponse;
import com.example.routemaker.domain.reward.entity.Stamp;
import com.example.routemaker.domain.reward.entity.UserStamp;
import com.example.routemaker.domain.reward.repository.StampRepository;
import com.example.routemaker.domain.reward.repository.UserStampRepository;
import com.example.routemaker.domain.user.entity.User;
import com.example.routemaker.domain.user.repository.UserRepository;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardService {

    private final StampRepository stampRepository;
    private final UserStampRepository userStampRepository;
    private final UserRepository userRepository;
    private final ChakApiClient chakApiClient;

    @Transactional
    public StampResponse grantStamp(String email, Long stampId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Stamp stamp = stampRepository.findById(stampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스탬프를 찾을 수 없습니다."));

        UserStamp userStamp = UserStamp.builder()
                .user(user)
                .stamp(stamp)
                .earnedAt(LocalDateTime.now())
                .build();
        userStampRepository.save(userStamp);

        return StampResponse.from(stamp);
    }

    public ShareCardResponse createShareCard(String email, Long courseId) {
        // TODO: ChakApiClient 연동 및 공유 카드 URL 생성
        return ShareCardResponse.builder()
                .shareUrl("https://routemaker.app/share/" + courseId)
                .title("RouteMaker 여행 완주 카드")
                .imageUrl(null)
                .build();
    }
}
