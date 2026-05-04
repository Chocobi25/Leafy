package com.chocobi.leafy.place.fetcher.theme;

import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ThemeService {
    private ThemeClient themeClient;
    private ThemeMapper themeMapper;

    public List<PlaceStaging> getPlaceStaging() {
        return themeClient.fetchThemePlaces().getResponse().getBody().getItems().getItem().stream()
                .map(themeMapper::toStaging)
                .toList();
    }
}
