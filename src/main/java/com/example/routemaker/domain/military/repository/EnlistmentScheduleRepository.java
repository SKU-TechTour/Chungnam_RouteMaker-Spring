package com.example.routemaker.domain.military.repository;

import com.example.routemaker.domain.military.entity.EnlistmentSchedule;
import com.example.routemaker.domain.military.enums.MilitaryBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EnlistmentScheduleRepository extends JpaRepository<EnlistmentSchedule, Long> {

    @Query("""
            select schedule
            from EnlistmentSchedule schedule
            where (:branch is null or schedule.militaryBranch = :branch)
              and (:fromDate is null or schedule.enlistmentDate >= :fromDate)
              and (:toDate is null or schedule.enlistmentDate <= :toDate)
            order by schedule.enlistmentDate asc, schedule.id asc
            """)
    List<EnlistmentSchedule> search(
            @Param("branch") MilitaryBranch branch,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
