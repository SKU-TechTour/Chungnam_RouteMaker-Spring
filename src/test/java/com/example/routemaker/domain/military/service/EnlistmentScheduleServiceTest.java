package com.example.routemaker.domain.military.service;

import com.example.routemaker.domain.military.enums.MilitaryBranch;
import com.example.routemaker.domain.military.repository.EnlistmentScheduleRepository;
import com.example.routemaker.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnlistmentScheduleServiceTest {

    @Mock
    private EnlistmentScheduleRepository enlistmentScheduleRepository;

    @InjectMocks
    private EnlistmentScheduleService enlistmentScheduleService;

    @Test
    void 군종과_기간으로_일정을_조회한다() {
        LocalDate fromDate = LocalDate.of(2026, 9, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);
        when(enlistmentScheduleRepository.search(MilitaryBranch.ARMY, fromDate, toDate))
                .thenReturn(List.of());

        assertThat(enlistmentScheduleService.getSchedules("army", fromDate, toDate)).isEmpty();

        verify(enlistmentScheduleRepository).search(MilitaryBranch.ARMY, fromDate, toDate);
    }

    @Test
    void 지원하지_않는_군종은_거부한다() {
        assertThatThrownBy(() -> enlistmentScheduleService.getSchedules("SPACE_FORCE", null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("군 구분");

        verifyNoInteractions(enlistmentScheduleRepository);
    }

    @Test
    void 시작일이_종료일보다_늦으면_거부한다() {
        LocalDate fromDate = LocalDate.of(2026, 12, 31);
        LocalDate toDate = LocalDate.of(2026, 9, 1);

        assertThatThrownBy(() -> enlistmentScheduleService.getSchedules(null, fromDate, toDate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("시작일");

        verifyNoInteractions(enlistmentScheduleRepository);
    }
}
