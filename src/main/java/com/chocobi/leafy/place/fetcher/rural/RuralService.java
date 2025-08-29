package com.chocobi.leafy.place.fetcher.rural;

import com.chocobi.leafy.place.entity.PlaceStaging;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RuralService {
    private final RuralClient ruralClient;
    private final RuralMapper ruralMapper;

    public List<PlaceStaging> getPlaceStaging() {
        return ruralClient.fetchRuralPlaces().getResponse().getBody().getItems().getItem().stream()
                .map(ruralMapper::toStaging)
                .toList();
    }
}
