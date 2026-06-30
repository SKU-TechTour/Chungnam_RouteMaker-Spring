package com.example.routemaker.domain.reward.repository;

import com.example.routemaker.domain.reward.entity.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<Stamp, Long> {
}
