package com.chocobi.leafy.place.dto.common;

import lombok.Data;

import java.util.List;

@Data
public class Items<T> {
    private List<T> item;
}
