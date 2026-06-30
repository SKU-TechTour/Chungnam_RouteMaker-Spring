package com.example.routemaker.global.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeUtil {

    public long minutesUntil(LocalDateTime target) {
        return java.time.Duration.between(LocalDateTime.now(), target).toMinutes();
    }
}
