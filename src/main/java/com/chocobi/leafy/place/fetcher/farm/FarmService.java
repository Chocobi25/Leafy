package com.chocobi.leafy.place.fetcher.farm;

import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmDetailItem;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmListItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class FarmService {
    private final FarmClient farmClient;
    private final FarmMapper farmMapper;

    public List<PlaceStaging> getPlaceStaging() {
        List<FarmListItem> listItems = farmClient.fetchFarmList().getBody().getFarmListItems().getItem();
        return listItems.stream()
                .map(listItem -> {
                    String contentId = listItem.getCntntsNo();

                    if (contentId == null) {
                        return null;
                    }

                    try {
                        FarmDetailItem detailItem = farmClient.fetchFarmDetail(contentId).getBody().getFarmDetailItem();

                        if (detailItem == null) {
                            return null;
                        }
                        return farmMapper.toStaging(detailItem);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
