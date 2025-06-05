package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class PlaceApiResponse<T> {
    private Response<T> response;
}
