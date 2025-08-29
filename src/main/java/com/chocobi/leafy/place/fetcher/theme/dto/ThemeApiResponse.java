package com.chocobi.leafy.place.fetcher.theme.dto;

import com.chocobi.leafy.place.common.dto.Response;
import lombok.Data;

@Data
public class ThemeApiResponse<T> {
    private Response<T> response;
}
