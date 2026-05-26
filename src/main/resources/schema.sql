DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    id                  bigint PRIMARY KEY,
    name                varchar(20),
    birth               date,
    email               varchar(255),
    nickname            varchar(12) NOT NULL,
    profile_image_url   varchar(255) NOT NULL,
    selected_level_icon varchar(20) NOT NULL,
    total_carbon_saved  double NOT NULL,
    level               enum('LV1','LV2','LV3','LV4','LV5') NOT NULL,
    role                enum('ADMIN','USER') NOT NULL,
    provider            enum('KAKAO','NAVER') NOT NULL,
    provider_id         varchar(255) NOT NULL,
    deleted_at          datetime(6),
    created_at          datetime(6),
    updated_at          datetime(6),
    unique (provider, provider_id)
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

DROP TABLE IF EXISTS region;
CREATE TABLE region
(
    id        bigint PRIMARY KEY,
    code      varchar(255) NOT NULL UNIQUE,
    name      varchar(100) NOT NULL,
    full_name varchar(200) NOT NULL,
    parent_id bigint,
    level     varchar(20)  NOT NULL
);

DROP TABLE IF EXISTS place;
CREATE TABLE place
(
    id          bigint PRIMARY KEY,
    place_type  varchar(31) NOT NULL,
    title       varchar(255) NOT NULL,
    address     varchar(255) NOT NULL,
    latitude    double       NOT NULL,
    longitude   double       NOT NULL,
    copyright   varchar(255),
    created_at  datetime(6),
    updated_at  datetime(6)
);

DROP TABLE IF EXISTS category_entity;
CREATE TABLE category_entity
(
    id       bigint PRIMARY KEY,
    code     varchar(255) NOT NULL UNIQUE,
    name     varchar(255) NOT NULL UNIQUE,
    icon_url varchar(255)
);

DROP TABLE IF EXISTS external_place;
CREATE TABLE external_place
(
    id          bigint PRIMARY KEY,
    description TEXT,
    tel         varchar(255),
    url         varchar(2000),
    category_id bigint,
    region_id   bigint
);

DROP TABLE IF EXISTS custom_place;
CREATE TABLE custom_place
(
    id      bigint PRIMARY KEY
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
    user_id           bigint NOT NULL,
    title       varchar(50) NOT NULL,
    status      enum('COMPLETED','CREATING','DRAFT','IN_PROGRESS','READY'),
    arrival_region_id bigint NOT NULL,
    departure_region_id bigint NOT NULL,
    carbon_emission double NOT NULL,
    carbon_saved double NOT NULL,
    route_stale boolean NOT NULL DEFAULT false,
    start_date date NOT NULL,
    end_date date NOT NULL,
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
    start_trip_place_id  bigint,
    end_trip_place_id    bigint,
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
    updated_at  datetime(6) NOT NULL
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
