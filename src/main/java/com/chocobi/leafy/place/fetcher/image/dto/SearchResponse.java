package com.chocobi.leafy.place.fetcher.image.dto;


import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private int total;
    private int start;
    private int display;
    private List<ImageItem> items;
}
