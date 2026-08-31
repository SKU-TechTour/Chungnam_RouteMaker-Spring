package com.example.routemaker.domain.military.service;

import com.example.routemaker.domain.military.dto.EnlistmentScheduleResponse;
import com.example.routemaker.domain.military.entity.EnlistmentSchedule;
import com.example.routemaker.domain.military.enums.MilitaryBranch;
import com.example.routemaker.domain.military.repository.EnlistmentScheduleRepository;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnlistmentScheduleService {

    private final EnlistmentScheduleRepository enlistmentScheduleRepository;

    public List<EnlistmentScheduleResponse> getSchedules(String branch, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        MilitaryBranch militaryBranch = parseBranch(branch);

        return enlistmentScheduleRepository.search(militaryBranch, fromDate, toDate).stream()
                .map(EnlistmentScheduleResponse::from)
                .toList();
    }

    public EnlistmentScheduleResponse getSchedule(Long scheduleId) {
        EnlistmentSchedule schedule = enlistmentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "입영 일정을 찾을 수 없습니다."));
        return EnlistmentScheduleResponse.from(schedule);
    }

    private MilitaryBranch parseBranch(String branch) {
        if (branch == null || branch.isBlank()) {
            return null;
        }

        try {
            return MilitaryBranch.valueOf(branch.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "군 구분은 ARMY, NAVY, AIR_FORCE, MARINE_CORPS 중 하나여야 합니다."
            );
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "조회 시작일은 종료일보다 늦을 수 없습니다.");
        }
    }
}
