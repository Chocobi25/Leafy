package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class PlaceResponse<T> {
    private Response<T> response;
}
