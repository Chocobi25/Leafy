package com.chocobi.leafy.place.dto.area;

import com.chocobi.leafy.place.dto.common.Response;
import lombok.Data;

@Data
public class AreaCodeApiResponse<T> {
    private Response<T> response;
}
