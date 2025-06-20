package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class ThemeApiResponse<T> {
    private Response<T> response;
}
