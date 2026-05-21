package com.chocobi.leafy.global.service;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionCommandService {
    private final RegionRepository regionRepository;

    public void save(RegionEntity region) {
        regionRepository.save(region);
    }

    public List<RegionEntity> saveAll(List<RegionEntity> regions) {
        return regionRepository.saveAll(regions);
    }
}
