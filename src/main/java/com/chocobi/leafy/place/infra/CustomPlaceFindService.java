package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.place.infra.repository.CustomPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomPlaceFindService {
    private final CustomPlaceRepository customPlaceRepository;
}
