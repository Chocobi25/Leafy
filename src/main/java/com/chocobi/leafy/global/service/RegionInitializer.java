package com.chocobi.leafy.global.service;

import com.chocobi.leafy.external.vworld.client.VWorldRegionClient;
import com.chocobi.leafy.external.vworld.dto.VWorldRegionResponse.VWorldRegionItem;
import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionLevel;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionInitializer {
    private final VWorldRegionClient vWorldRegionClient;
    private final RegionCommandService regionCommandService;
    private final RegionFindService regionFindService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initRegions() {
        if (regionFindService.findRegionCount() > 0) {
            log.info("이미 지역 데이터가 존재합니다.");
            return;
        }

        log.info("행정구역 데이터 초기화 시작");

        initSido();
        initSigungu();
        initDong();
        initRee();

        log.info("행정구역 데이터 초기화 완료");
    }

    @Transactional
    public void initSido() {
        initRegionLevel(
                RegionLevel.SIDO,
                null
        );
    }

    @Transactional
    public void initSigungu() {
        initRegionLevel(
                RegionLevel.SIGUNGU,
                RegionLevel.SIDO
        );
    }

    @Transactional
    public void initDong() {
        initRegionLevel(
                RegionLevel.EMD,
                RegionLevel.SIGUNGU
        );
    }

    @Transactional
    public void initRee() {
        initRegionLevel(
                RegionLevel.REE,
                RegionLevel.EMD
        );
    }

    private void initRegionLevel(RegionLevel level, RegionLevel parentLevel) {
        log.info("{} 데이터 초기화 시작", level.getDescription());

        List<VWorldRegionItem> items;

        if (parentLevel == null) {
            items = vWorldRegionClient.fetchSidos();
        } else {
            List<RegionEntity> parentRegions = regionFindService.findRegions(parentLevel);
            if (parentRegions.isEmpty()) {
                log.warn("{} 데이터가 없습니다. 먼저 {} 데이터를 초기화해주세요.",
                        parentLevel.getDescription(), parentLevel.getDescription());
                return;
            }

            items = fetchChildRegions(level, parentRegions);
        }

        int count = saveRegionsWithParent(items, level, parentLevel);
        log.info("{} 데이터 {}건 저장 완료", level.getDescription(), count);
    }

    private List<VWorldRegionItem> fetchChildRegions(RegionLevel level, List<RegionEntity> parentRegions) {
        List<VWorldRegionItem> result = new ArrayList<>();
        int count = 0;
        int total = parentRegions.size();

        for (RegionEntity parent : parentRegions) {
            count++;
            try {
                List<VWorldRegionItem> temp = fetchChildRegionsByLevel(level, parent.getCode());

                if (temp != null && !temp.isEmpty()) {
                    result.addAll(temp);
                    log.debug("{} 하위 데이터 {}건 조회 (code: {})",
                            parent.getName(), temp.size(), parent.getCode());
                } else {
                    log.debug("{} 하위 데이터 없음 (code: {})",
                            parent.getName(), parent.getCode());
                }

                Thread.sleep(100);

            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    log.warn("{} 조회 중 인터럽트 발생 ({}번째, code: {})", parent.getName(), count, parent.getCode());
                    break;
                }

                log.warn("{} 조회 실패 ({}번째, code: {}): {}",
                        parent.getName(), count, parent.getCode(), e.getMessage());
            }
        }

        log.info("총 {}건 수집 완료 ({}/{}개 지역 조회)", result.size(), count, total);
        return result;
    }

    private List<VWorldRegionItem> fetchChildRegionsByLevel(RegionLevel level, String parentCode) {
        return switch (level) {
            case SIGUNGU -> vWorldRegionClient.fetchSigungus(parentCode);
            case EMD -> vWorldRegionClient.fetchEmds(parentCode);
            case REE -> vWorldRegionClient.fetchRees(parentCode);
            case SIDO -> throw new IllegalArgumentException("SIDO는 하위 지역 조회 대상이 아닙니다.");
        };
    }

    private int saveRegionsWithParent(List<VWorldRegionItem> regionInfos, RegionLevel level, RegionLevel parentLevel) {
        if (regionInfos.isEmpty()) {
            log.warn("저장할 데이터가 없습니다.");
            return 0;
        }

        Map<String, RegionEntity> parentMap = new HashMap<>();
        if (parentLevel != null) {
            List<RegionEntity> parents = regionFindService.findRegions(parentLevel);
            parents.forEach(parent -> parentMap.put(parent.getCode(), parent));
        }

        List<RegionEntity> entities = regionInfos.stream()
                .map(info -> {
                    String parentCode = getParentCode(info.getAdmCode(), level);
                    RegionEntity parent = parentCode != null ? parentMap.get(parentCode) : null;
                    return toEntity(info, level, parent);
                })
                .toList();

        List<RegionEntity> savedRegions = regionCommandService.saveAll(entities);
        return savedRegions.size();
    }

    private String getParentCode(String admCode, RegionLevel level) {
        if (admCode == null) return null;

        return switch (level) {
            case SIDO -> null;
            case SIGUNGU -> admCode.substring(0, 2);
            case EMD -> admCode.substring(0, 5);
            case REE -> admCode.substring(0, 8);
        };
    }

    private RegionEntity toEntity(VWorldRegionItem item, RegionLevel level, RegionEntity parent) {
        String rawName = item.getAdmCodeNm() != null ? item.getAdmCodeNm() : item.getLowestAdmCodeNm();
        String parsedName = extractLastName(rawName);

        return RegionEntity.builder()
                .code(item.getAdmCode())
                .name(parsedName)
                .fullName(rawName)
                .level(level)
                .parent(parent)
                .build();
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) return fullName;

        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
