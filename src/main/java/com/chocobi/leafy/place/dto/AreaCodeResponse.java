package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class AreaCodeResponse<T> {
    private Response<T> response;
}
