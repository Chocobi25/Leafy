package com.chocobi.leafy.place.common.dto;

import lombok.Data;

@Data
public class Body<T> {
    private int numOfRows;
    private int pageNo;
    private int totalCount;
    private Items<T> items;
}
