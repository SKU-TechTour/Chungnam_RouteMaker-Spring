package com.example.routemaker.domain.military.service;

import com.example.routemaker.domain.military.dto.SafeTimeResponse;
import com.example.routemaker.domain.military.entity.MilitaryUnit;
import com.example.routemaker.domain.military.repository.MilitaryUnitRepository;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import com.example.routemaker.global.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafeTimeService {

    private static final int RETURN_BUFFER_MINUTES = 30;

    private final MilitaryUnitRepository militaryUnitRepository;
    private final DistanceUtil distanceUtil;

    public SafeTimeResponse calculateSafeTime(Long unitId, double currentLat, double currentLng, int returnDeadlineMinutes) {
        MilitaryUnit unit = militaryUnitRepository.findById(unitId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "부대 정보를 찾을 수 없습니다."));

        double distanceKm = distanceUtil.calculateDistanceKm(
                currentLat, currentLng, unit.getLatitude(), unit.getLongitude()
        );
        int travelMinutes = distanceUtil.estimateTravelMinutes(distanceKm);
        int remainingMinutes = returnDeadlineMinutes - travelMinutes;
        boolean safe = remainingMinutes >= RETURN_BUFFER_MINUTES;

        return SafeTimeResponse.builder()
                .unitName(unit.getName())
                .remainingMinutes(remainingMinutes)
                .travelMinutes(travelMinutes)
                .safe(safe)
                .build();
    }
}
