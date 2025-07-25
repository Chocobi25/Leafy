package com.chocobi.leafy.place.dto.eco;

import com.chocobi.leafy.place.dto.common.Response;
import lombok.Data;

@Data
public class EcoApiResponse<T> {
    private Response<T> response;
}
