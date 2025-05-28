package com.chocobi.leafy.place.dto;

import lombok.Data;

import java.util.List;

@Data
public class Items<T> {
    private List<T> item;
}
