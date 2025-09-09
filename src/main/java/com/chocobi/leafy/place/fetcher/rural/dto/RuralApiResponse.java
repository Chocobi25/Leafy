package com.chocobi.leafy.place.fetcher.rural.dto;

import com.chocobi.leafy.place.common.dto.Response;
import lombok.Data;

@Data
public class RuralApiResponse<T> {
    private Response<T> response;
}
