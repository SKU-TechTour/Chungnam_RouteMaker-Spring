package com.example.routemaker.global.util;

import org.springframework.stereotype.Component;

@Component
public class DistanceUtil {

    public double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        // TODO: Haversine 공식 등으로 GPS 거리 계산
        return 0.0;
    }

    public int estimateTravelMinutes(double distanceKm) {
        // TODO: 이동 수단별 이동 시간 추정
        return 0;
    }
}
