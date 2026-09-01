# Chungnam RouteMaker — Spring Backend

> **Frontend**: Flutter (MVVM + Feature-first)  
> **Backend**: Spring (DDD / Layered Architecture)  

---

## Github Guidline

| Prefix | 용도 | 예시 상황 |
|--------|------|-----------|
| `feat` | 새로운 기능(Feature) 추가 | API, 비즈니스 로직, 스케줄러 등 **동작이 추가**될 때 |
| `fix` | 버그·에러 수정(Fix) | 잘못된 동작, 장애, 데이터 오류 등 **문제 해결** |
| `chore` | 빌드·인프라·설정 | 패키지, 환경 변수, 키 파일, Gradle 등 **기능과 무관한 유지보수** |
| `docs` | 문서(Documentation) | README, API 명세, 아키텍처 문서 등 |
| `refactor` | 구조 개선(Refactor) | **기능은 동일**, 코드 정리·분리·이름 변경 |
| `style` | 포맷·린트 | 세미콜론, import 정리 등 **로직 변경 없음** |

#### feat — 새 기능

```
feat: 맞춤형 3단 코스 추천 API 추가
```

#### fix — 버그 수정

```
fix: JWT 만료 시 401 대신 500이 반환되던 문제 수정
```

#### chore — 설정 변경

```
chore: Cloudtype 배포 환경 변수 정리
```

#### docs — 문서

```
docs: Spring 아키텍처 및 Flutter 연동 가이드 README 추가
```

#### refactor — 구조 개선 (기능 동일)

```
refactor: PlaceService 필터 로직을 PlaceFilterService로 분리
```

---

# 1. 아키텍처 개요

충남 지역(논산·공주·부여) 여행 코스 추천 앱 **RouteMaker**의 Spring Boot 백엔드입니다.

Flutter가 **클라이언트(UI·GPS·지도)**, Spring이 **비즈니스 로직·DB·외부 API 연동·인증**을 담당하는 **클라이언트–서버(C/S) 구조**입니다.

```
┌─────────────────┐         HTTPS/HTTP (REST + JSON)         ┌──────────────────────────┐
│  Flutter App    │  ──────────────────────────────────────► │  Spring Boot Backend     │
│  (Mobile)       │  ◄────────────────────────────────────── │  (routemaker_backend)    │
└─────────────────┘         ApiResponse<T> JSON              └───────────┬──────────────┘
                                                                           │
                                    ┌──────────────────────────────────────┴──────────────────────┐
                                    │                                                             │
                                    ▼                                                             ▼
                             PostgreSQL                                                       외부 API
                             (사용자·일정)                                      (기상청, TourAPI, 카카오)
```

| 구분 | 역할 |
|------|------|
| **Flutter** | 화면, 사용자 입력, GPS/지도, JWT 로컬 저장, API 호출 |
| **Spring** | REST API, 인증/인가, 도메인 규칙, DB CRUD, 외부 API 중계 |
| **PostgreSQL** | User, 입영 일정, Stamp 등 서비스 고유 영속 데이터 |

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Web | Spring Web MVC (REST) |
| ORM | Spring Data JPA / Hibernate |
| DB | PostgreSQL 15 |
| Security | Spring Security + Firebase ID Token (Stateless) |
| Build | Gradle |
| Infra | Docker Compose (`postgres`) |

---

## 병무청 입영 일정 API

입영 일정은 PostgreSQL의 `enlistment_schedule` 테이블에 저장하고 모든 응답은
`ApiResponse<T>` 형식으로 반환합니다. 최초 실행 시 Flyway가 테이블과 개발용
수동 데이터 20건을 생성합니다.

```http
GET /api/military/enlistment-schedules
GET /api/military/enlistment-schedules?branch=ARMY&fromDate=2026-09-01&toDate=2026-12-31
GET /api/military/enlistment-schedules/{scheduleId}
```

`branch`는 `ARMY`, `NAVY`, `AIR_FORCE`, `MARINE_CORPS` 중 하나입니다. 초기 20건은
화면 및 API 개발을 위한 수동 데이터이므로 운영 반영 전 병무청 공식 공고와
날짜를 반드시 대조해야 합니다.

로컬 인프라는 다음 명령으로 실행합니다.

```bash
docker compose up -d
./gradlew bootRun
```

환경별로 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를
지정할 수 있으며, 지정하지 않으면 `docker-compose.yml`의 로컬 기본값을 사용합니다.

## 관광·날씨·카카오 API 연동

### 공모전 OpenAPI 준수 원칙

- 관광지 목록·소개·운영정보·반려동물 정보는 요청 시 한국관광공사 OpenAPI를 실시간 호출합니다.
- TourAPI 응답을 PostgreSQL이나 애플리케이션 메모리에 저장·캐싱하지 않습니다.
- `GET /api/places`는 DB의 `places` 테이블이 아니라 `areaBasedList2`를 호출합니다.
- `POST /api/courses/recommend`도 DB 장소 대신 `areaBasedList2`의 실시간 후보를 사용합니다.
- 코스는 기상청 예보로 실내·실외 장소를 고르고 카카오모빌리티 실시간 차량 거리·시간을 합칩니다.
- 반려동물 필터를 켜면 각 후보의 `detailPetTour2`를 실시간 조회합니다.
- 외부 API 실패를 로컬 관광지 데이터로 조용히 대체하지 않고 오류로 반환합니다.
- 기존 장소 엔티티/시드는 레거시 스키마 호환용이며 운영 관광정보·추천 응답에 사용하지 않습니다.

API 키는 Git에 저장하지 않고 실행 환경에만 넣습니다. 채팅, 이슈, README 등에 노출된
키는 카카오 디벨로퍼스에서 재발급한 뒤 새 키를 사용하세요.

```powershell
$env:TOUR_API_SERVICE_KEY="공공데이터포털 일반 인증키"
$env:WEATHER_API_SERVICE_KEY="공공데이터포털 일반 인증키"
$env:KAKAO_REST_API_KEY="카카오 REST API 키"
.\gradlew.bat bootRun
```

서버는 인증키를 Flutter에 노출하지 않습니다. 앱의 지역 관광지 목록은 아래 공개 프록시를
사용하며, 호출할 때마다 TourAPI 호출 내역이 발생합니다.

```http
GET /api/places?region=NONSAN
GET /api/places?region=GONGJU&petFriendly=true
GET /api/places?region=BUYEO&category=HERITAGE
```

아래 진단용 API는 JWT 인증 후 호출합니다.

```http
GET /api/external/tour/search?keyword=공산성
GET /api/external/tour/operating-info?contentId={id}&contentTypeId={type}
GET /api/external/tour/pet-info?contentId={id}
GET /api/external/weather/short-term?baseDate=20260831&baseTime=1400&nx=62&ny=97
GET /api/external/kakao/places?query=논산 맛집&categoryGroupCode=FD6
GET /api/external/kakao/driving-route?originPlaceId=1&destinationPlaceId=2
```

TourAPI의 `detailIntro2`는 관광지·문화시설·음식점별로 서로 다른 필드명과 자유문자
영업 정보를 반환합니다. 백엔드는 이를 `restDay`, `openTime`, `parking`, `contact`로
정규화하지만, 문장을 임의로 파싱해 `영업 중`이라고 단정하지 않습니다. 화면에는
원문과 최신 확인 안내를 보여주고 방문 전 전화/공식 페이지 확인을 함께 안내합니다.
반려동물 정보는 같은 TourAPI의 `detailPetTour2`를 사용합니다.

사용자 현재 GPS와 관광지 간 직선거리는 Flutter의 `geolocator`로 기기 안에서만
계산합니다. 카카오 차량 길찾기는 임의 좌표 대신 DB에 등록된 `originPlaceId`와
`destinationPlaceId`만 받으므로 현재 GPS를 Spring 서버로 전송하지 않습니다.

### Firebase Authentication 연결

Flutter는 Google 로그인 또는 익명 로그인 후 Firebase ID 토큰을
`Authorization: Bearer <ID_TOKEN>` 헤더로 전송합니다. Spring은 Firebase Admin SDK로
서명, 만료 시간, 발급 프로젝트를 검증한 뒤 `firebase_uid`를 로컬 사용자와 연결합니다.

로컬 실행 전 Firebase Console의 **프로젝트 설정 > 서비스 계정 > 새 비공개 키 생성**에서
받은 JSON을 저장소 바깥에 보관하고 다음 환경변수를 설정합니다. 앱용
`google-services.json`은 서버 비공개 키가 아니므로 대신 사용할 수 없습니다.

```powershell
[Environment]::SetEnvironmentVariable(
  'FIREBASE_SERVICE_ACCOUNT_PATH',
  'C:\secure\chungnam-routemaker-service-account.json',
  'User'
)
[Environment]::SetEnvironmentVariable(
  'FIREBASE_PROJECT_ID',
  'chungnam-routemaker',
  'User'
)
```

새 터미널과 Spring 실행 구성을 다시 시작해야 환경변수가 반영됩니다. 인증 정보는
첫 인증 요청 시 지연 로딩되므로, 서비스 계정이 없어도 공개 관광 API 서버는 기동됩니다.

---

## 아키텍처 설계 원칙

### DDD + Layered Architecture + Package by Feature

교과서적인 4계층(`presentation / application / domain / infrastructure`)을 **패키지 전체에 반복**하기보다, **기능(도메인) 단위로 패키지를 나누고**, 각 도메인 안에 **Layered 계층**을 두는 **실용형 구조**입니다.

```
요청 흐름 (한 도메인 내부)

  Flutter HTTP Request
        │
        ▼
  controller/     ← Presentation: REST 엔드포인트, 요청/응답 DTO 변환
        │
        ▼
  service/        ← Application: 유스케이스·비즈니스 규칙 조합
        │
        ├── repository/  ← Infrastructure(Persistence): JPA DB 접근
        ├── entity/      ← Domain Model: DB 테이블 매핑 객체
        └── dto/         ← API 전용 입출력 객체 (Entity 직접 노출 X)
```

### `global/` vs `domain/` 분리

| 패키지 | 역할 |
|--------|------|
| **`global/`** | 모든 도메인이 공유하는 **기술·인프라** (Security, CORS, 예외, 공통 응답, 외부 API Client) |
| **`domain/`** | **비즈니스 기능 단위** (user, place, course, military, reward) |

**규칙**

- 도메인 간 참조: `course → place` (조회), `reward → user` (조회)처럼 **단방향** 유지
- Entity는 API로 직접 반환하지 않고 **Response DTO**로 변환
- 외부 API(Tour, 기상청)는 `global/client` 또는 도메인 전용 `client/`에서 호출

---

## 패키지 구조

```
src/main/java/com/example/routemaker/
│
├── RoutemakerApplication.java          # Spring Boot 진입점
│
├── global/                             # 전역 공통
│   ├── config/                         # Security, CORS, JPA, Swagger
│   ├── security/                       # JWT Filter, UserDetailsService
│   ├── exception/                      # GlobalExceptionHandler, ErrorCode
│   ├── response/                       # ApiResponse<T> 공통 응답 래퍼
│   ├── client/                         # WeatherApiClient, TourApiClient
│   ├── common/enums/                   # Region (NONSAN, GONGJU, BUYEO)
│   └── util/                           # JwtUtil, DistanceUtil, DateTimeUtil
│
└── domain/
    ├── user/                           # 회원 · 인증 · 마이페이지
    │   ├── auth/                       # 회원가입/로그인 (user 하위)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   └── dto/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    ├── place/                          # 장소 (유적지/맛집/카페)
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   ├── dto/
    │   └── enums/                      # PlaceCategory
    │
    ├── course/                         # 코스 추천 · 3단 콤보 · Plan B
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    ├── military/                       # 군인 Safe-Time · 복귀 시간
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    └── reward/                         # 스탬프 · 뱃지 · 공유 카드
        ├── controller/
        ├── service/
        ├── repository/
        ├── entity/
        ├── dto/
        └── client/                     # ChakApiClient
```
