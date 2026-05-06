-- 1. 유저 데이터 (Users)
INSERT INTO users (kakao_id, nickname, profile_image_url, selected_level_icon, total_carbon_saved, level, role, created_at, updated_at)
VALUES
(12345678, '초코비', 'https://example.com/profiles/1.png', 1, 25.5, 'LV3', 'USER', NOW(), NOW()),
(87654321, '관리자', 'https://example.com/profiles/admin.png', 2, 100.0, 'LV5', 'ADMIN', NOW(), NOW()),
(11223344, '테스터', null, 1, 0.0, 'LV1', 'USER', NOW(), NOW());

-- 2. 유저 디바이스 (User Device)
INSERT INTO user_device (id, user_id, fcm_token, created_at, updated_at)
VALUES
(1, 12345678, 'fcm_token_sample_123', NOW(), NOW()),
(2, 87654321, 'fcm_token_sample_admin', NOW(), NOW());

-- 3. 지역 데이터 먼저 삽입 (ID: 1, 2, 3...)
INSERT INTO region (id, name, parent_id, level) VALUES (1, '서울', NULL, 'SIDO');
INSERT INTO region (id, name, parent_id, level) VALUES (2, '부산', NULL, 'SIDO');
INSERT INTO region (id, name, parent_id, level) VALUES (3, '제주', NULL, 'SIDO');

-- 4. 부모 테이블 (place) 데이터 삽입
INSERT INTO place (id, title, address, latitude, longitude) VALUES
(1, '남산서울타워', '서울 용산구 남산공원길 105', 37.55117, 126.9882),
(2, '해운대 해수욕장', '부산 해운대구 해운대해변로 264', 35.1587, 129.1604);

-- 4. 자식 장소 테이블 (external_place)
INSERT INTO external_place (id, description, url, tel, region) VALUES
(1, '서울의 랜드마크입니다.', 'https://place.com/1', '02-123-4567', 2),
(2, '여름철 최고의 휴양지입니다.', 'https://place.com/2', '051-987-6543', 1);

-- 5. 이미지 데이터 (Image)
INSERT INTO image (id, place_id, url, copyright)
VALUES
(1, 1, 'https://images.com/namsan1.jpg', '한국관광공사'),
(2, 1, 'https://images.com/namsan2.jpg', 'ⓒ초코비'),
(3, 2, 'https://images.com/haeundae.jpg', '부산시청');

-- 6. 게시글 데이터 (Post)
INSERT INTO post (id, place_id, user_id, likes, content, title, created_at, updated_at)
VALUES
(1, 1, 12345678, 10, '야경이 정말 예뻐요!', '남산 나들이', NOW(), NOW()),
(2, 2, 11223344, 5, '사람이 많지만 바다가 깨끗해요.', '부산 여행 후기', NOW(), NOW());

-- 7. 여행 계획 (Trip)
INSERT INTO trip (id, user_kakao_id, title, status, arrival, departure, carbon_emission, carbon_saved, start_date, end_date, created_at, updated_at)
VALUES
(1, 12345678, '서울 힐링 여행', 'COMPLETED', 'SEOUL', 'BUSAN', 12.5, 5.2, '2026-02-01', '2026-02-03', NOW(), NOW()),
(2, 12345678, '제주 한달살기', 'READY', 'JEJU', 'SEOUL', 45.0, 10.0, '2026-03-01', '2026-03-31', NOW(), NOW());

-- 8. 여행지 상세 (Trip Place)
INSERT INTO trip_place (id, place_id, trip_id, day_index, visit_order, memo)
VALUES
(1, 1, 1, 0, 1, '도착하자마자 가기'),
(2, 3, 1, 0, 2, '한복 대여해서 사진 찍기');

-- 9. 여행 구간 정보 (Trip Segment)
INSERT INTO trip_segment (id, trip_id, start_place_id, end_place_id, distance, duration, carbon_emitted, transport, created_at, updated_at)
VALUES
(1, 1, 1, 3, 5.2, 20, 1.2, 'BUS', NOW(), NOW());
