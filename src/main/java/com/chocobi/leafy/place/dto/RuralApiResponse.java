package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class RuralApiResponse<T> {
    private Response<T> response;
}
