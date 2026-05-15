package com.chocobi.leafy.global.service;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionRepository;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.global.exception.GlobalError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionFindService {
    private final RegionRepository regionRepository;

    public RegionEntity findRegion(String name) {
        return regionRepository.findByName(name)
                .orElseThrow(() -> new CustomException(GlobalError.REGION_NOT_FOUND));
    }

    public RegionEntity findRegion(Long id) {
        return regionRepository.findById(id).orElse(null);
    }

    public List<RegionEntity> findRegions() {
        return regionRepository.findAll();
    }
}
