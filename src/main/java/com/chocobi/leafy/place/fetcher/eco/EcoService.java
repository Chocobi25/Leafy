package com.chocobi.leafy.place.fetcher.eco;

import com.chocobi.leafy.place.entity.PlaceStaging;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EcoService {
    private final EcoClient ecoClient;
    private final EcoMapper ecoMapper;

    public List<PlaceStaging> getPlaceStaging() {
        return ecoClient.fetchEcoPlaces().getResponse().getBody().getItems().getItem().stream()
                .map(ecoMapper::toStaging)
                .toList();
    }
}
