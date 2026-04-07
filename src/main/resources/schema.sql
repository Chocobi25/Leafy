DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    kakao_id            bigint PRIMARY KEY,
    nickname            varchar(12) NOT NULL,
    profile_image_url   varchar(255),
    selected_level_icon tinyint,
    total_carbon_saved  double,
    level               enum('LV1','LV2','LV3','LV4','LV5') NOT NULL,
    role                enum('ADMIN','USER') NOT NULL,
    created_at          datetime(6),
    updated_at          datetime(6)
);

DROP TABLE IF EXISTS user_device;
CREATE TABLE user_device
(
    id         bigint PRIMARY KEY,
    user_id    bigint NOT NULL,
    fcm_token  varchar(255) NOT NULL,
    created_at          datetime(6) NOT NULL,
    updated_at          datetime(6) NOT NULL
);

DROP TABLE IF EXISTS place;
CREATE TABLE place
(
    id              bigint PRIMARY KEY,
    latitude        double NOT NULL,
    longitude       double NOT NULL,
    url             varchar(2000),
    address         varchar(255) NOT NULL,
    copyright       varchar(255),
    region_detail   varchar(255),
    tel             varchar(255),
    title           varchar(255) NOT NULL,
    category        enum('CULTURE','ETC','EXPERIENCE','FOOD','NATURE'),
    description     TEXT,
    region_group    enum('BUSAN','CHUNGBUK','CHUNGNAM','DAEGU','DAEJEON','GANGWON','GWANGJU','GYEONGBUK','GYEONGGI','GYEONGNAM','INCHEON','JEJU','JEONBUK','JEONNAM','SEJONG','SEOUL','ULSAN'),
    source_type     enum('API','USER')
);

DROP TABLE IF EXISTS image;
CREATE TABLE image
(
    id          bigint PRIMARY KEY,
    place_id    bigint,
    url         varchar(2000),
    copyright   varchar(255)
);

DROP TABLE IF EXISTS trip;
CREATE TABLE trip
(
    id                bigint PRIMARY KEY,
    user_kakao_id     bigint,
    title       varchar(255),
    status      enum('COMPLETED','CREATING','DRAFT','IN_PROGRESS','READY'),
    arrival     enum('BUSAN','CHUNGBUK','CHUNGNAM','DAEGU','DAEJEON','GANGWON','GWANGJU','GYEONGBUK','GYEONGGI','GYEONGNAM','INCHEON','JEJU','JEONBUK','JEONNAM','SEJONG','SEOUL','ULSAN'),
    departure   enum('BUSAN','CHUNGBUK','CHUNGNAM','DAEGU','DAEJEON','GANGWON','GWANGJU','GYEONGBUK','GYEONGGI','GYEONGNAM','INCHEON','JEJU','JEONBUK','JEONNAM','SEJONG','SEOUL','ULSAN'),
    carbon_emission double NOT NULL,
    carbon_saved double NOT NULL,
    start_date date,
    end_date date,
    certification_at    datetime(6),
    created_at  datetime(6) NOT NULL,
    updated_at  datetime(6) NOT NULL
);

DROP TABLE IF EXISTS trip_place;
CREATE TABLE trip_place
(
    id              bigint PRIMARY KEY,
    place_id        bigint,
    trip_id         bigint,
    day_index       int NOT NULL,
    visit_order     int NOT NULL,
    memo            varchar(255)
);

DROP TABLE IF EXISTS trip_segment;
CREATE TABLE trip_segment
(
    id              bigint PRIMARY KEY,
    trip_id         bigint,
    start_place_id  bigint,
    end_place_id    bigint,
    distance        double NOT NULL,
    duration        int NOT NULL,
    carbon_emitted  double,
    max_carbon_emission double,
    transport       varchar(255),
    created_at  datetime(6) NOT NULL,
    updated_at  datetime(6) NOT NULL
);

DROP TABLE IF EXISTS user_place;
CREATE TABLE user_place
(
    id          bigint PRIMARY KEY,
    address     varchar(255) NOT NULL,
    place_url   varchar(255),
    title       varchar(255),
    type        enum('API','USER'),
    latitude    double NOT NULL,
    longitude   double NOT NULL
);

CREATE TABLE post
(
    id          bigint PRIMARY KEY,
    place_id    bigint,
    user_id     bigint       NOT NULL,
    likes       int          NOT NULL DEFAULT 0,
    view_count  int          NOT NULL DEFAULT 0,
    title       varchar(255),
    content     text,
    created_at  datetime(6)  NOT NULL,
    updated_at  datetime(6)  NOT NULL
);

DROP TABLE IF EXISTS post_like;
CREATE TABLE post_like
(
    id          bigint PRIMARY KEY,
    post_id     bigint NOT NULL,
    user_id     bigint NOT NULL,
    created_at  datetime(6) NOT NULL,
    updated_at  datetime(6) NOT NULL,
);

DROP TABLE IF EXISTS post_comment;
CREATE TABLE post_comment
(
    id          bigint PRIMARY KEY,
    post_id     bigint       NOT NULL,
    user_id     bigint       NOT NULL,
    parent_id   bigint,
    content     varchar(255) NOT NULL,
    created_at  datetime(6)  NOT NULL,
    updated_at  datetime(6)  NOT NULL
);

DROP TABLE IF EXISTS area_code;
CREATE TABLE area_code
(
    code     int PRIMARY KEY,
    name     varchar(255)
);