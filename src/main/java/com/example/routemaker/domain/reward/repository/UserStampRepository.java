package com.example.routemaker.domain.reward.repository;

import com.example.routemaker.domain.reward.entity.UserStamp;
import com.example.routemaker.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {

    List<UserStamp> findByUser(User user);
}
