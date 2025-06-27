package com.chocobi.leafy.place.dto.theme;

import com.chocobi.leafy.place.dto.common.Response;
import lombok.Data;

@Data
public class ThemeApiResponse<T> {
    private Response<T> response;
}
