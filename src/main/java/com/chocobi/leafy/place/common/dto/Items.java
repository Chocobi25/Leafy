package com.chocobi.leafy.place.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class Items<T> {
    private List<T> item;
}
