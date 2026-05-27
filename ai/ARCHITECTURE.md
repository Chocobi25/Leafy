# ARCHITECTURE


## 1. 프로젝트 개요

이 프로젝트 친환경 저탄소 배출 여행 설계 서비스 "Leafy"이다. 
관심사의 분리를 달성하고 유지보수성을 높이기 위해 **Layered Architecture** 형태로 리팩토링 중이다.
각 계층은 **상위 계층에서 하위 계층으로의 단방향 의존성**만 가진다. 역방향 의존성(하위 계층이 상위 계층을 참조하는 것)이나 계층을 건너뛰는 의존성은 금지한다.

---

## 2. 계층별 역할 및 구성 요소

### 1) Presentation Layer
사용자의 요청(HTTP Request)을 받아들이고 시스템의 진입점을 정의한다.
- **구성 요소:**
  - `Controller`: 응답 반환을 처리, 비즈니스 로직은 절대 포함하지 않는다.
  - `Docs`: API 명세 및 대외 소통을 위한 오픈API 문서화 레이어

### 2) Application Layer
비즈니스 시나리오를 흐름대로 제어하고 트랜잭션 경계를 관리한다.
- **구성 요소:**
  - `Service`: 외부에서 들어온 DTO 데이터를 기반으로 필요한 엔티티들을 일괄 조회하고, 도메인 객체에 핵심 명령을 내린 뒤 결과를 반환하는 **선언적 스타일**을 지향.

### 3) Infrastructure Layer
데이터의 영속성(Persistence)과 도메인의 생명주기 및 조회/수정 책임을 나누어 관리하는 핵심 계층이다.
- **구성 요소:**
  - `Entity`: 데이터베이스 테이블과 매핑되는 객체이자, 비즈니스 규칙과 상태 변경 메서드를 가진다.
  - `Repository`: Spring Data JPA 기반의 물리적 DB 접근 인터페이스.
  - `FindService` : 조회를 담당하는 서비스. `@Transactional(readOnly = true)` 환경에서 작동.
  - `CommandService` : **등록, 수정, 삭제 등 데이터의 상태 변경을 전담**하는 서비스.

### 4) DTO Layer
계층 간 데이터 이동 시 데이터 격리를 위해 철저히 분리된 객체를 사용하며, 엔티티는 절대로 외부 Presentation 레이어로 노출하지 않는다.
- **구성 요소:**
  - `Request`: 클라이언트가 전송한 입력을 받는 DTO. Record 형태로 작성하고 @Schema를 통해 설명 추가 및 검증
  - `Response`: 클라이언트에게 결과를 반환하는 DTO.

### 5) VO Layer
도메인의 특정한 값을 표현하거나, 시스템 전반에서 공통으로 사용되는 불변 객체와 스펙을 모아둔다.
- **예:**
  - `Error (Enum / Class)`: 비즈니스 예외 처리 시 발생하는 에러 코드와 메시지를 정의. `/global/ErrorCode`를 상속해서 작성.

---

## 3. 도메인 개념 및 데이터 모델

### 1) User
서비스를 이용하는 회원 도메인이다.
- **주요 모델:** `UserEntity`
- **주요 속성:** OAuth 제공자 정보(`provider`, `providerId`), 프로필(`nickname`, `profileImageUrl`), 권한(`role`), 레벨(`level`, `selectedLevelIcon`), 누적 탄소 절감량(`totalCarbonSaved`)
- **주요 규칙:** `provider + providerId` 조합은 유일하며, 탈퇴 시 `deletedAt`을 기록하는 Soft Delete 방식을 사용한다.
- **연관 관계:** 사용자는 여행(`Trip`), 게시글(`Post`), 댓글, 좋아요, FCM 디바이스의 소유자가 된다.

### 2) Place
여행 경로에 포함될 수 있는 장소 도메인이다.
- **주요 모델:** `PlaceEntity`, `ExternalPlaceEntity`, `CustomPlaceEntity`, `PlaceImageEntity`, `CategoryEntity`
- **주요 속성:** 장소명, 주소, 위도/경도, 저작권, 이미지, 카테고리, 지역 정보
- **주요 규칙:** `PlaceEntity`를 기준으로 외부 API 기반 장소(`ExternalPlaceEntity`)와 사용자/관리자 생성 장소(`CustomPlaceEntity`)를 JOINED 상속 구조로 분리한다.
- **연관 관계:** 장소는 여행 일정의 방문 지점(`TripPlace`)으로 연결되고, 게시글은 `placeId`를 통해 장소를 참조한다.

### 3) Trip
사용자가 설계하고 인증하는 저탄소 여행 계획 도메인이다.
- **주요 모델:** `TripEntity`, `TripPlaceEntity`, `TripSegmentEntity`
- **주요 속성:** 제목, 여행 기간, 출발/도착 지역, 상태(`TripStatus`), 탄소 배출량(`carbonEmission`), 탄소 절감량(`carbonSaved`), 인증 시각(`certificationAt`)
- **주요 규칙:** 여행은 반드시 한 명의 사용자에게 속하며, 장소 목록은 일차(`dayIndex`)와 방문 순서(`visitOrder`)를 가진다. 구간(`TripSegment`)은 이동수단, 거리, 소요 시간, 탄소 배출량을 기록한다.
- **연관 관계:** `Trip`은 `User`와 N:1, `TripPlace` 및 `TripSegment`와 1:N 관계를 가진다.

### 4) Post
사용자가 여행 또는 장소 경험을 공유하는 커뮤니티 도메인이다.
- **주요 모델:** `PostEntity`, `PostCommentEntity`, `PostLikeEntity`
- **주요 속성:** 제목, 본문, 작성자, 장소 ID, 조회수, 좋아요 수
- **주요 규칙:** 게시글은 한 명의 작성자에게 속하며, 좋아요는 `user_id + post_id` 조합으로 중복을 방지한다. 댓글은 `parent`를 통해 대댓글 구조를 지원한다.
- **연관 관계:** `Post`는 `User`와 N:1, 댓글 및 좋아요와 1:N 관계를 가진다.

---

## 4. 패키지 구조

- 각각의 도메인 패키지 내부에서 presentation, application, infra, DTO, VO 레이어를 하위 패키지로 분리하여 관리한다.
  - 이를 통해 하나의 도메인 기능 변경이 다른 도메인에 영향을 주지 않도록 격리한다.
- 시스템 전반에 걸쳐 공유되는 공통 설정 및 예외 처리는 global 패키지로 일원화한다.
- 외부 API 연동은 external 패키지에 공급자별로 분리해 client, dto 하위 패키지를 만들어 관리한다.
