package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.CourseRecommendRequest;
import com.example.routemaker.domain.course.dto.CourseResponse;
import com.example.routemaker.domain.course.dto.HourlyWeatherResponse;
import com.example.routemaker.domain.course.dto.RouteLegResponse;
import com.example.routemaker.domain.course.dto.RouteCoordinateResponse;
import com.example.routemaker.domain.course.dto.RouteGuideResponse;
import com.example.routemaker.domain.course.dto.RoutePointRequest;
import com.example.routemaker.domain.course.dto.RoutePreviewRequest;
import com.example.routemaker.domain.course.dto.RoutePreviewResponse;
import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.global.client.kakao.KakaoMobilityApiClient;
import com.example.routemaker.global.client.tour.TourApiClient;
import com.example.routemaker.global.client.tour.dto.TourPlaceResponse;
import com.example.routemaker.global.client.weather.WeatherApiClient;
import com.example.routemaker.global.common.enums.Region;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final String CHUNGNAM_AREA_CODE = "34";
    private static final String TOURIST_ATTRACTION = "12";
    private static final String CULTURAL_FACILITY = "14";
    private static final String RESTAURANT = "39";
    private static final String ACCOMMODATION = "32";
    private static final AtomicInteger API_THREAD_SEQUENCE = new AtomicInteger();

    private final TourApiClient tourApiClient;
    private final WeatherApiClient weatherApiClient;
    private final KakaoMobilityApiClient kakaoMobilityApiClient;
    private final ExecutorService externalApiExecutor = Executors.newFixedThreadPool(6, task -> {
        Thread thread = new Thread(task, "course-api-" + API_THREAD_SEQUENCE.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    @PreDestroy
    void shutdownExternalApiExecutor() {
        externalApiExecutor.shutdown();
    }

    public CourseResponse recommendCourse(CourseRecommendRequest request) {
        if (request.getRegion() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지역을 선택해야 합니다.");
        }
        return compose(request.getRegion(), request.isMilitary(), request.getConcepts(),
                request.getVariant(), request.getRouteTemplate());
    }

    public List<CourseResponse> recommendCourses(CourseRecommendRequest request) {
        if (request.getRegion() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지역을 선택해야 합니다.");
        }
        PreparedCourseData prepared = prepare(
                request.getRegion(), request.isMilitary(), request.getConcepts(),
                request.getRouteTemplate());
        return java.util.stream.IntStream.range(0, 5)
                .mapToObj(variant -> async(() ->
                        buildCourse(prepared, variant, request.getRouteTemplate())))
                .map(this::await)
                .sorted(Comparator.comparingInt(CourseResponse::getTotalDurationSeconds))
                .toList();
    }

    public CourseResponse shufflePlanB(Long courseId) {
        int regionCode = Math.toIntExact(courseId / 1000);
        if (regionCode < 1 || regionCode > Region.values().length) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "코스 ID가 올바르지 않습니다.");
        }
        Region region = Region.values()[regionCode - 1];
        int nextVariant = Math.toIntExact(courseId % 1000) + 1;
        return compose(region, false, Set.of(), nextVariant, null);
    }

    public RoutePreviewResponse previewRoute(RoutePreviewRequest request) {
        if (request == null || request.spots() == null
                || request.spots().size() < 2 || request.spots().size() > 12) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "경로 장소는 2~12곳이어야 합니다.");
        }
        List<RouteLegResponse> routes = new ArrayList<>();
        for (int index = 0; index < request.spots().size() - 1; index++) {
            RoutePointRequest origin = request.spots().get(index);
            RoutePointRequest destination = request.spots().get(index + 1);
            KakaoMobilityApiClient.DrivingRoute route = kakaoMobilityApiClient.detailedDirections(
                    origin.longitude(), origin.latitude(),
                    destination.longitude(), destination.latitude());
            routes.add(RouteLegResponse.builder()
                    .originPlaceId(origin.id())
                    .destinationPlaceId(destination.id())
                    .distanceMeters(route.distanceMeters())
                    .durationSeconds(route.durationSeconds())
                    .tollWon(route.tollWon())
                    .taxiFareWon(route.taxiFareWon())
                    .path(route.path().stream()
                            .map(point -> new RouteCoordinateResponse(point.latitude(), point.longitude()))
                            .toList())
                    .guides(route.guides().stream()
                            .map(guide -> new RouteGuideResponse(
                                    guide.instruction(), guide.latitude(), guide.longitude(),
                                    guide.distanceMeters(), guide.durationSeconds()))
                            .toList())
                    .source("KAKAO_MOBILITY_REALTIME")
                    .build());
        }
        int distance = routes.stream().mapToInt(RouteLegResponse::getDistanceMeters).sum();
        int duration = routes.stream().mapToInt(RouteLegResponse::getDurationSeconds).sum();
        return new RoutePreviewResponse(List.copyOf(routes), distance, duration,
                "KAKAO_MOBILITY_REALTIME");
    }

    private CourseResponse compose(Region region, boolean military, Set<String> concepts,
                                   int variant, String routeTemplate) {
        return buildCourse(prepare(region, military, concepts, routeTemplate), variant, routeTemplate);
    }

    private PreparedCourseData prepare(Region region, boolean military, Set<String> concepts,
                                       String routeTemplate) {
        List<HourlyWeatherResponse> hourlyWeather = weatherApiClient.hourly(region.name());
        boolean rainy = hourlyWeather.stream().anyMatch(HourlyWeatherResponse::precipitationExpected);
        String attractionType = rainy ? CULTURAL_FACILITY : TOURIST_ATTRACTION;

        CompletableFuture<List<TourPlaceResponse>> attractionsFuture = async(() ->
                tourApiClient.areaBased(
                        CHUNGNAM_AREA_CODE, sigunguCode(region), attractionType, 80));
        CompletableFuture<List<TourPlaceResponse>> diningFuture = async(() ->
                tourApiClient.areaBased(
                        CHUNGNAM_AREA_CODE, sigunguCode(region), RESTAURANT, 100));

        List<TourPlaceResponse> attractions = await(attractionsFuture);
        if (attractions.isEmpty()) {
            attractions = tourApiClient.areaBased(
                    CHUNGNAM_AREA_CODE, sigunguCode(region), TOURIST_ATTRACTION, 80);
        }
        List<TourPlaceResponse> dining = await(diningFuture);
        List<TourPlaceResponse> accommodations = List.of();

        if ("COMPANION_OVERNIGHT_A".equals(routeTemplate)) {
            CompletableFuture<List<TourPlaceResponse>> buyeoAttractionsFuture = async(() ->
                    tourApiClient.areaBased(
                            CHUNGNAM_AREA_CODE, sigunguCode(Region.BUYEO), TOURIST_ATTRACTION, 80));
            CompletableFuture<List<TourPlaceResponse>> gongjuAttractionsFuture = async(() ->
                    tourApiClient.areaBased(
                            CHUNGNAM_AREA_CODE, sigunguCode(Region.GONGJU), TOURIST_ATTRACTION, 80));
            CompletableFuture<List<TourPlaceResponse>> buyeoDiningFuture = async(() ->
                    tourApiClient.areaBased(
                            CHUNGNAM_AREA_CODE, sigunguCode(Region.BUYEO), RESTAURANT, 100));
            CompletableFuture<List<TourPlaceResponse>> gongjuDiningFuture = async(() ->
                    tourApiClient.areaBased(
                            CHUNGNAM_AREA_CODE, sigunguCode(Region.GONGJU), RESTAURANT, 100));
            CompletableFuture<List<TourPlaceResponse>> accommodationsFuture = async(() ->
                    tourApiClient.areaBased(
                            CHUNGNAM_AREA_CODE, sigunguCode(Region.BUYEO), ACCOMMODATION, 80));

            List<TourPlaceResponse> combinedAttractions = new ArrayList<>(attractions);
            combinedAttractions.addAll(await(buyeoAttractionsFuture));
            combinedAttractions.addAll(await(gongjuAttractionsFuture));
            attractions = combinedAttractions;
            List<TourPlaceResponse> combinedDining = new ArrayList<>(dining);
            combinedDining.addAll(await(buyeoDiningFuture));
            combinedDining.addAll(await(gongjuDiningFuture));
            dining = combinedDining;
            accommodations = await(accommodationsFuture);
        } else if ("COMPANION_OVERNIGHT_B".equals(routeTemplate)) {
            accommodations = tourApiClient.areaBased(
                    CHUNGNAM_AREA_CODE, sigunguCode(Region.NONSAN), ACCOMMODATION, 80);
        }

        attractions = filterByConcepts(attractions, concepts);
        if (region == Region.NONSAN && military) {
            attractions = nearestToTrainingCenter(attractions);
            dining = nearestToTrainingCenter(dining);
        }

        return new PreparedCourseData(
                region, military, concepts == null ? Set.of() : concepts,
                rainy, hourlyWeather, attractions, dining, accommodations);
    }

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, externalApiExecutor);
    }

    private <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw exception;
        }
    }

    private CourseResponse buildCourse(PreparedCourseData prepared, int variant,
                                       String requestedTemplate) {
        Region region = prepared.region();
        boolean military = prepared.military();
        Set<String> concepts = prepared.concepts();
        boolean rainy = prepared.rainy();
        List<HourlyWeatherResponse> hourlyWeather = prepared.hourlyWeather();
        List<TourPlaceResponse> attractions = prepared.attractions();
        List<TourPlaceResponse> dining = prepared.dining();
        List<TourPlaceResponse> accommodations = prepared.accommodations();

        TourPlaceResponse attraction = pick(attractions, variant, "관광지");
        List<TourPlaceResponse> cafes = dining.stream().filter(this::isCafe).toList();
        List<TourPlaceResponse> restaurants = dining.stream().filter(place -> !isCafe(place)).toList();
        if (restaurants.isEmpty()) restaurants = dining;
        if (cafes.isEmpty()) cafes = dining;
        TourPlaceResponse restaurant = pick(restaurants.isEmpty() ? dining : restaurants, variant, "맛집");
        TourPlaceResponse cafe = pick(cafes.isEmpty() ? dining : cafes, variant + 1, "카페");
        TourPlaceResponse anotherRestaurant = pick(
                restaurants.isEmpty() ? dining : restaurants, variant + 1, "맛집");
        TourPlaceResponse anotherCafe = pick(
                cafes.isEmpty() ? dining : cafes, variant + 2, "카페");

        String template = requestedTemplate == null || requestedTemplate.isBlank()
                ? (military ? "LEGACY_MILITARY" : "TRAVELER_FLEX")
                : requestedTemplate;
        List<PlaceResponse> combo = new ArrayList<>();
        if ("ENLISTEE_A".equals(template)) {
            combo.add(place(restaurant, Region.NONSAN, null));
            combo.add(place(cafe, Region.NONSAN, null));
            combo.add(PlaceResponse.nonsanTrainingCenter());
        } else if ("ENLISTEE_B".equals(template)) {
            combo.add(place(restaurant, Region.NONSAN, null));
            combo.add(PlaceResponse.nonsanTrainingCenter());
        } else if ("ENLISTEE_C".equals(template)) {
            combo.add(place(cafe, Region.NONSAN, null));
            combo.add(place(restaurant, Region.NONSAN, null));
            combo.add(PlaceResponse.nonsanTrainingCenter());
        } else if ("COMPANION_DAY_A".equals(template)) {
            addDayStops(combo, region, variant, attractions, restaurants, cafes,
                    List.of("R", "T", "H", "C", "R", "H"));
        } else if ("COMPANION_DAY_B".equals(template)) {
            addDayStops(combo, region, variant, attractions, restaurants, cafes,
                    List.of("R", "T", "C", "H", "R"));
        } else if ("COMPANION_DAY_C".equals(template)) {
            addDayStops(combo, region, variant, attractions, restaurants, cafes,
                    List.of("R", "T", "H", "C", "R"));
        } else if ("COMPANION_OVERNIGHT_A".equals(template)) {
            buildOvernightA(combo, variant, attractions, dining, accommodations);
        } else if ("COMPANION_OVERNIGHT_B".equals(template)) {
            buildOvernightB(combo, variant, attractions, restaurants, cafes, accommodations);
        } else if (region == Region.NONSAN && military) {
            combo.add(PlaceResponse.fromTour(attraction, region, false, false));
            combo.add(PlaceResponse.nonsanTrainingCenter());
            combo.add(PlaceResponse.fromTour(
                    variant % 2 == 0 ? restaurant : cafe, region, false, false));
        } else {
            combo.add(PlaceResponse.fromTour(attraction, region, false, false));
            boolean cafeFocused = concepts.contains("cafe") && !concepts.contains("food");
            boolean foodFocused = concepts.contains("food") && !concepts.contains("cafe");
            if (cafeFocused) {
                combo.add(PlaceResponse.fromTour(cafe, region, false, false));
                combo.add(PlaceResponse.fromTour(anotherCafe, region, false, false));
            } else if (foodFocused) {
                combo.add(PlaceResponse.fromTour(restaurant, region, false, false));
                combo.add(PlaceResponse.fromTour(anotherRestaurant, region, false, false));
            } else {
                combo.add(PlaceResponse.fromTour(restaurant, region, false, false));
                combo.add(PlaceResponse.fromTour(cafe, region, false, false));
            }
        }

        List<RouteLegResponse> routes = buildRoutes(combo);
        int totalDistance = routes.stream().mapToInt(RouteLegResponse::getDistanceMeters).sum();
        int totalDuration = routes.stream().mapToInt(RouteLegResponse::getDurationSeconds).sum();
        long id = (region.ordinal() + 1L) * 1000L + Math.floorMod(variant, 1000);
        String recommendedStartTime = null;
        String targetArrivalTime = null;
        if (region == Region.NONSAN && military) {
            targetArrivalTime = "13:00";
            int trainingIndex = java.util.stream.IntStream.range(0, combo.size())
                    .filter(index -> combo.get(index).getId() == -1L)
                    .findFirst()
                    .orElse(0);
            int travelSecondsBeforeTraining = routes.stream()
                    .limit(trainingIndex)
                    .mapToInt(RouteLegResponse::getDurationSeconds)
                    .sum();
            recommendedStartTime = LocalTime.of(13, 0)
                    .minusMinutes(trainingIndex * 60L)
                    .minusSeconds(travelSecondsBeforeTraining)
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        return CourseResponse.builder()
                .id(id)
                .title(title(region, military, rainy, concepts, template))
                .region(region)
                .indoor(rainy)
                .weather(rainy ? "RAINY" : "CLEAR")
                .hourlyWeather(hourlyWeather)
                .recommendedStartTime(recommendedStartTime)
                .targetArrivalTime(targetArrivalTime)
                .combo(List.copyOf(combo))
                .routes(routes)
                .totalDistanceMeters(totalDistance)
                .totalDurationSeconds(totalDuration)
                .source("TOUR_API_REALTIME+WEATHER_API_REALTIME+KAKAO_MOBILITY_REALTIME")
                .build();
    }

    private record PreparedCourseData(
            Region region,
            boolean military,
            Set<String> concepts,
            boolean rainy,
            List<HourlyWeatherResponse> hourlyWeather,
            List<TourPlaceResponse> attractions,
            List<TourPlaceResponse> dining,
            List<TourPlaceResponse> accommodations
    ) {
    }

    private List<RouteLegResponse> buildRoutes(List<PlaceResponse> combo) {
        List<RouteLegResponse> routes = new ArrayList<>();
        for (int index = 0; index < combo.size() - 1; index++) {
            PlaceResponse origin = combo.get(index);
            PlaceResponse destination = combo.get(index + 1);
            KakaoMobilityApiClient.DrivingRoute route = kakaoMobilityApiClient.directions(
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude());
            routes.add(RouteLegResponse.builder()
                    .originPlaceId(origin.getId())
                    .destinationPlaceId(destination.getId())
                    .distanceMeters(route.distanceMeters())
                    .durationSeconds(route.durationSeconds())
                    .tollWon(route.tollWon())
                    .taxiFareWon(route.taxiFareWon())
                    .source("KAKAO_MOBILITY_REALTIME")
                    .build());
        }
        return List.copyOf(routes);
    }

    private TourPlaceResponse pick(List<TourPlaceResponse> places, int variant, String label) {
        List<TourPlaceResponse> usable = places.stream()
                .filter(place -> place.latitude() != 0 && place.longitude() != 0)
                .toList();
        if (usable.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, label + " 실시간 관광정보를 찾을 수 없습니다.");
        }
        return usable.get(Math.floorMod(variant, usable.size()));
    }

    private boolean isCafe(TourPlaceResponse place) {
        String name = place.name().toLowerCase();
        return "A05020900".equals(place.categoryCode())
                || name.contains("카페") || name.contains("커피")
                || name.contains("베이커리") || name.contains("빵");
    }

    private List<TourPlaceResponse> filterByConcepts(
            List<TourPlaceResponse> places, Set<String> concepts) {
        if (concepts == null || concepts.isEmpty()) return places;
        boolean filtersAttractions = concepts.contains("healing")
                || concepts.contains("activity") || concepts.contains("history");
        if (!filtersAttractions) return places;
        List<TourPlaceResponse> filtered = places.stream().filter(place -> {
            String category = place.categoryCode();
            return (concepts.contains("healing") && category.startsWith("A01"))
                    || (concepts.contains("history") && category.startsWith("A02"))
                    || (concepts.contains("activity") && category.startsWith("A03"));
        }).toList();
        return filtered.isEmpty() ? places : filtered;
    }

    private List<TourPlaceResponse> nearestToTrainingCenter(List<TourPlaceResponse> places) {
        final double trainingLat = 36.1119731;
        final double trainingLng = 127.1083526;
        return places.stream()
                .sorted(Comparator.comparingDouble(place ->
                        Math.pow(place.latitude() - trainingLat, 2)
                                + Math.pow(place.longitude() - trainingLng, 2)))
                .limit(15)
                .toList();
    }

    private String title(Region region, boolean military, boolean rainy, Set<String> concepts,
                         String template) {
        String regionName = switch (region) {
            case NONSAN -> "논산";
            case GONGJU -> "공주";
            case BUYEO -> "부여";
        };
        if (template.startsWith("ENLISTEE_")) return "13시 입소 맞춤 " + template.substring(9) + " 코스";
        if (template.startsWith("COMPANION_DAY_")) return "배웅 후 당일 " + template.substring(14) + " 코스";
        if ("COMPANION_OVERNIGHT_A".equals(template)) return "부여·공주 백제 1박 2일 코스";
        if ("COMPANION_OVERNIGHT_B".equals(template)) return "논산 로컬 힐링 1박 2일 코스";
        if (region == Region.NONSAN && military) return "육군훈련소 입영 전후 맞춤 코스";
        if (rainy) {
            return regionName + " 비 오는 날 실내 맞춤 코스";
        }
        if (concepts != null) {
            if (concepts.contains("history")) return regionName + " 역사 중심 맞춤 코스";
            if (concepts.contains("activity")) return regionName + " 액티비티 중심 맞춤 코스";
            if (concepts.contains("healing")) return regionName + " 힐링 중심 맞춤 코스";
            if (concepts.contains("food") && !concepts.contains("cafe")) {
                return regionName + " 맛집 중심 맞춤 코스";
            }
            if (concepts.contains("cafe") && !concepts.contains("food")) {
                return regionName + " 카페 중심 맞춤 코스";
            }
        }
        return regionName + " 취향 맞춤 코스";
    }

    private PlaceResponse place(TourPlaceResponse source, Region fallback, String schedule) {
        return PlaceResponse.fromTour(source, regionFrom(source, fallback), false, false, schedule);
    }

    private Region regionFrom(TourPlaceResponse place, Region fallback) {
        String address = place.address() == null ? "" : place.address();
        if (address.contains("부여")) return Region.BUYEO;
        if (address.contains("공주")) return Region.GONGJU;
        if (address.contains("논산")) return Region.NONSAN;
        return fallback;
    }

    private void addDayStops(List<PlaceResponse> combo, Region region, int variant,
                             List<TourPlaceResponse> attractions,
                             List<TourPlaceResponse> restaurants,
                             List<TourPlaceResponse> cafes, List<String> pattern) {
        int h = 0, r = 0, c = 0;
        for (String kind : pattern) {
            switch (kind) {
                case "T" -> combo.add(PlaceResponse.nonsanTrainingCenter());
                case "H" -> combo.add(place(pick(attractions, variant + h++, "유적지"), region, null));
                case "C" -> combo.add(place(pick(cafes.isEmpty() ? restaurants : cafes,
                        variant + c++, "카페"), region, null));
                default -> combo.add(place(pick(restaurants, variant + r++, "맛집"), region, null));
            }
        }
    }

    private void buildOvernightA(List<PlaceResponse> combo, int variant,
                                 List<TourPlaceResponse> attractions,
                                 List<TourPlaceResponse> dining,
                                 List<TourPlaceResponse> accommodations) {
        List<TourPlaceResponse> nonsanDining = inRegion(dining, "논산");
        List<TourPlaceResponse> buyeoDining = inRegion(dining, "부여");
        List<TourPlaceResponse> gongjuDining = inRegion(dining, "공주");
        List<TourPlaceResponse> buyeoHeritage = inRegion(attractions, "부여");
        List<TourPlaceResponse> gongjuHeritage = inRegion(attractions, "공주");
        List<TourPlaceResponse> buyeoCafes = buyeoDining.stream().filter(this::isCafe).toList();
        List<TourPlaceResponse> gongjuCafes = gongjuDining.stream().filter(this::isCafe).toList();
        combo.add(place(pick(nonsanDining.isEmpty() ? dining : nonsanDining, variant, "논산 맛집"), Region.NONSAN, "1일차 11:00~12:30"));
        combo.add(PlaceResponse.nonsanTrainingCenter());
        combo.add(place(pickPreferred(buyeoHeritage, variant, "부여 유적지", "부소산성", "낙화암"), Region.BUYEO, "1일차 13:00~15:30"));
        combo.add(place(pick(buyeoCafes.isEmpty() ? buyeoDining : buyeoCafes, variant, "부여 카페"), Region.BUYEO, "1일차 15:30~17:00"));
        combo.add(place(pick(buyeoDining, variant + 1, "부여 맛집"), Region.BUYEO, "1일차 17:00~19:00"));
        combo.add(place(pick(accommodations, variant, "부여 숙소"), Region.BUYEO, "1일차 19:00 이후"));
        combo.add(place(pickPreferred(gongjuHeritage, variant, "공주 유적지", "공산성"), Region.GONGJU, "2일차 10:00~11:30"));
        combo.add(place(pick(gongjuDining, variant, "공주 맛집"), Region.GONGJU, "2일차 11:30~13:00"));
        combo.add(place(pick(gongjuCafes.isEmpty() ? gongjuDining : gongjuCafes, variant + 1, "공주 카페"), Region.GONGJU, "2일차 13:00~14:30"));
    }

    private void buildOvernightB(List<PlaceResponse> combo, int variant,
                                 List<TourPlaceResponse> attractions,
                                 List<TourPlaceResponse> restaurants,
                                 List<TourPlaceResponse> cafes,
                                 List<TourPlaceResponse> accommodations) {
        combo.add(place(pick(restaurants, variant, "논산 맛집"), Region.NONSAN, "1일차 11:00~12:30"));
        combo.add(PlaceResponse.nonsanTrainingCenter());
        combo.add(place(pickPreferred(cafes.isEmpty() ? restaurants : cafes, variant, "탑정호 카페", "탑정호"), Region.NONSAN, "1일차 13:00~15:00"));
        combo.add(place(pickPreferred(attractions, variant, "논산 유적지", "선샤인랜드"), Region.NONSAN, "1일차 15:00~17:00"));
        combo.add(place(pick(restaurants, variant + 1, "논산 맛집"), Region.NONSAN, "1일차 17:00~19:00"));
        combo.add(place(pick(accommodations, variant, "논산 숙소"), Region.NONSAN, "1일차 19:00 이후"));
        combo.add(place(pickPreferred(attractions, variant + 1, "논산 유적지", "돈암서원"), Region.NONSAN, "2일차 10:30~12:00"));
        combo.add(place(pick(restaurants, variant + 2, "논산 맛집"), Region.NONSAN, "2일차 12:00~13:30"));
        combo.add(place(pick(cafes.isEmpty() ? restaurants : cafes, variant + 1, "논산 카페"), Region.NONSAN, "2일차 13:30~14:30"));
    }

    private List<TourPlaceResponse> inRegion(List<TourPlaceResponse> places, String region) {
        return places.stream().filter(place -> place.address() != null && place.address().contains(region)).toList();
    }

    private TourPlaceResponse pickPreferred(List<TourPlaceResponse> places, int variant,
                                            String label, String... keywords) {
        List<TourPlaceResponse> preferred = places.stream()
                .filter(place -> java.util.Arrays.stream(keywords)
                        .anyMatch(keyword -> place.name().contains(keyword)))
                .toList();
        return pick(preferred.isEmpty() ? places : preferred, variant, label);
    }

    private String sigunguCode(Region region) {
        return switch (region) {
            case GONGJU -> "1";
            case NONSAN -> "3";
            case BUYEO -> "6";
        };
    }
}
