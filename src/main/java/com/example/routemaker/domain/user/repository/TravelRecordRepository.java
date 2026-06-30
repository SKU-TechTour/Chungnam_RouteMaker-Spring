package com.example.routemaker.domain.user.repository;

import com.example.routemaker.domain.user.entity.TravelRecord;
import com.example.routemaker.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelRecordRepository extends JpaRepository<TravelRecord, Long> {

    List<TravelRecord> findByUserOrderByCompletedAtDesc(User user);
}
