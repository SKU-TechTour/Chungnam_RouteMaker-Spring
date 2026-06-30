package com.example.routemaker.domain.reward.repository;

import com.example.routemaker.domain.reward.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
