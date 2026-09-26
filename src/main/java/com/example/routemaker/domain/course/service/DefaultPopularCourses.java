package com.example.routemaker.domain.course.service;

import com.example.routemaker.domain.course.dto.BookmarkSpotRequest;
import com.example.routemaker.domain.course.dto.PopularCourseResponse;
import com.example.routemaker.global.common.enums.Region;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 찜 데이터가 아직 부족한 지역을 위한 고정 코스입니다. 인기 순위의 실제 찜 수와
 * 혼동되지 않도록 bookmarkCount=0, rankingBasis=기본 추천 코스로 반환합니다.
 * 장소 좌표와 TourAPI contentId는 실제 추천 응답에서 확인한 값입니다.
 */
final class DefaultPopularCourses {
    private DefaultPopularCourses() {}

    static List<PopularCourseResponse> forRegion(Region region) {
        return switch (region) {
            case NONSAN -> List.of(
                    route(region, "입영 전 논산 역사·맛집 코스",
                            spot("946844", "강경역사관", "HERITAGE", 36.1620834404, 127.0152089296,
                                    "충청남도 논산시 강경읍 계백로167번길 50"),
                            spot("2818602", "내동춘천닭갈비", "RESTAURANT", 36.1804745993, 127.1066558184,
                                    "충청남도 논산시 시민로132번길 52"),
                            spot("-1", "육군훈련소", "HERITAGE", 36.1119731, 127.1083526,
                                    "충남 논산시 연무읍 득안대로 504")),
                    route(region, "논산 강경 역사·카페 코스",
                            spot("1627011", "강경미내다리", "HERITAGE", 36.152857008, 127.031949343,
                                    "충청남도 논산시 채운면 삼거리"),
                            spot("135768", "김재성두부촌", "RESTAURANT", 36.1957058328, 127.0860989186,
                                    "충청남도 논산시 부창로 76"),
                            spot("2834584", "초서", "CAFE", 36.1741233185, 127.1507714876,
                                    "충청남도 논산시 탑정로 802")),
                    route(region, "논산 탑정호 맛집·카페 코스",
                            spot("946712", "강경역사문화안내소", "HERITAGE", 36.1610252177, 127.0146367865,
                                    "충청남도 논산시 강경읍 옥녀봉로27번길 30-5"),
                            spot("134197", "루체", "RESTAURANT", 36.1755232187, 127.1541933666,
                                    "충청남도 논산시 가야곡면 탑정로 840"),
                            spot("2829973", "뷰포인트카페", "CAFE", 36.17743977, 127.1601177865,
                                    "충청남도 논산시 탑정로 900")));
            case GONGJU -> List.of(
                    route(region, "공주 갑사·로컬 카페 코스",
                            spot("125891", "갑사", "HERITAGE", 36.3650187978, 127.1875769727,
                                    "충청남도 공주시 계룡면 갑사로 567-3"),
                            spot("134174", "고마나루1999", "RESTAURANT", 36.4650256418, 127.1227467571,
                                    "충청남도 공주시 백미고을길 5-8"),
                            spot("2752563", "루치아의 뜰", "CAFE", 36.4538297218, 127.1237997426,
                                    "충청남도 공주시 웅진로 145-8")),
                    route(region, "공주 곰나루·동학사 코스",
                            spot("2756160", "곰나루국민관광단지", "HERITAGE", 36.4702692908, 127.1117170502,
                                    "충청남도 공주시 백제큰길 2110-16"),
                            spot("2736661", "곰골식당", "RESTAURANT", 36.4518298957, 127.1206250807,
                                    "충청남도 공주시 봉황산1길 1-2"),
                            spot("2876970", "숲카페", "CAFE", 36.3598397118, 127.2373004369,
                                    "충청남도 공주시 동학사1로 256-26")),
                    route(region, "공주 공산성·감성 카페 코스",
                            spot("2916020", "공산성연지", "HERITAGE", 36.4639480871, 127.1284157218,
                                    "충청남도 공주시 금성동"),
                            spot("2929641", "까우", "RESTAURANT", 36.4717723292, 127.1363064692,
                                    "충청남도 공주시 번영3로 58"),
                            spot("2876999", "어썸845", "CAFE", 36.3594315497, 127.2443775913,
                                    "충청남도 공주시 동학사1로 209-8")));
            case BUYEO -> List.of(
                    route(region, "부여 구드래·백제 코스",
                            spot("126699", "구드래조각공원", "HERITAGE", 36.2872339058, 126.9068264344,
                                    "충청남도 부여군 부여읍 백강로 148"),
                            spot("2489102", "백제궁 수라간", "RESTAURANT", 36.3064541159, 126.9176971723,
                                    "충청남도 부여군 규암면 백제문로 555"),
                            spot("2832948", "비비비", "CAFE", 36.2954280769, 126.9450838984,
                                    "충청남도 부여군 삼충로 99")),
                    route(region, "부여 부소산·로컬 맛집 코스",
                            spot("125881", "고란약수", "HERITAGE", 36.2927361764, 126.9138867671,
                                    "충청남도 부여군 부여읍 부소로 31-5"),
                            spot("1797807", "백제고을 누룽지백숙", "RESTAURANT", 36.2781184511, 126.9165174739,
                                    "충청남도 부여군 부여읍 정림로 96"),
                            spot("2832885", "높은댕이", "CAFE", 36.3113208683, 126.9546520624,
                                    "충청남도 부여군 삼충로 325-34")),
                    route(region, "부여 임천·수북로 코스",
                            spot("1956297", "간곡서원", "HERITAGE", 36.1825403583, 126.90839295,
                                    "충청남도 부여군 임천면 성흥로283번길 31-4"),
                            spot("801453", "구드래황토정", "RESTAURANT", 36.2788622785, 126.8858702022,
                                    "충청남도 부여군 규암면 백제문로 30"),
                            spot("2717585", "수북로1945", "CAFE", 36.2722963914, 126.8874757872,
                                    "충청남도 부여군 규암면 수북로41번길 11-50")));
        };
    }

    private static PopularCourseResponse route(Region region, String title, BookmarkSpotRequest... spots) {
        List<BookmarkSpotRequest> routeSpots = List.of(spots);
        String routeKey = region.name() + ":" + routeSpots.stream()
                .map(BookmarkSpotRequest::id).collect(Collectors.joining("-"));
        return new PopularCourseResponse(routeKey, region.name(), title, routeSpots,
                0, 0, 0, 0, "기본 추천 코스");
    }

    private static BookmarkSpotRequest spot(String id, String name, String category,
                                            double latitude, double longitude, String address) {
        return new BookmarkSpotRequest(id, name, category, latitude, longitude,
                null, "CURATED_DEFAULT", address, null);
    }
}
