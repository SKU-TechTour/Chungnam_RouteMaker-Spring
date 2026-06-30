package com.example.routemaker.domain.place.repository;

import com.example.routemaker.domain.place.entity.Place;
import com.example.routemaker.domain.place.enums.PlaceCategory;
import com.example.routemaker.global.common.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByRegionAndCategory(Region region, PlaceCategory category);
}
