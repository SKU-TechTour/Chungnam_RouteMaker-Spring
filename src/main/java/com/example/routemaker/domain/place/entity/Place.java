package com.example.routemaker.domain.place.entity;

import com.example.routemaker.domain.place.enums.PlaceCategory;
import com.example.routemaker.global.common.enums.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceCategory category;

    private String address;

    private double latitude;

    private double longitude;

    private boolean strollerAccessible;

    private boolean petFriendly;

    private boolean largeParking;

    private boolean militaryDiscount;

    @Builder
    public Place(
            String name,
            Region region,
            PlaceCategory category,
            String address,
            double latitude,
            double longitude,
            boolean strollerAccessible,
            boolean petFriendly,
            boolean largeParking,
            boolean militaryDiscount
    ) {
        this.name = name;
        this.region = region;
        this.category = category;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.strollerAccessible = strollerAccessible;
        this.petFriendly = petFriendly;
        this.largeParking = largeParking;
        this.militaryDiscount = militaryDiscount;
    }
}
