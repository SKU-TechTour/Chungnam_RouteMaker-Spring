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
chore: Redis Docker Compose 설정 추가
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
                                    ┌──────────────────────────────────────┼──────────────────────┐
                                    │                                      │                      │
                                    ▼                                      ▼                      ▼
                             PostgreSQL                                 Redis              외부 API
                             (영속 데이터)                            (캐시, 예정)      (기상청, TourAPI, Chak)
```

| 구분 | 역할 |
|------|------|
| **Flutter** | 화면, 사용자 입력, GPS/지도, JWT 로컬 저장, API 호출 |
| **Spring** | REST API, 인증/인가, 도메인 규칙, DB CRUD, 외부 API 중계 |
| **PostgreSQL** | User, Place, Course, Stamp 등 영속 데이터 |
| **Redis** | 캐시 (날씨·TourAPI 등, 추후 활용 예정) |

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Web | Spring Web MVC (REST) |
| ORM | Spring Data JPA / Hibernate |
| DB | PostgreSQL 15 |
| Cache | Redis 7 |
| Security | Spring Security + JWT (Stateless) |
| Build | Gradle |
| Infra | Docker Compose (`postgres`, `redis`) |

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
│   ├── config/                         # Security, CORS, Redis, JPA, Swagger
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
