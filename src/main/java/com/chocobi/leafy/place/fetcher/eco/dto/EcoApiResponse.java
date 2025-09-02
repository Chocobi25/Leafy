package com.chocobi.leafy.place.fetcher.eco.dto;

import com.chocobi.leafy.place.common.dto.Response;
import lombok.Data;

@Data
public class EcoApiResponse<T> {
    private Response<T> response;
}
