package com.chocobi.leafy.trip.infra.entity;

public enum TripStatus {
    DRAFT,      // 임시저장 상태 (기존 데이터 호환용)
    CREATING,   // 여행 생성 중 (지역/날짜 선택, 관광지 선택 단계)
    READY,      // 여행 생성 완료 (일정 확정, 출발 전)
    IN_PROGRESS,// 여행 진행 중 (여행 날짜에 진입)
    COMPLETED   // 여행 종료 (여행 끝난 상태)
}
