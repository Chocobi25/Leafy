package com.chocobi.leafy.place.dto.rural;

import com.chocobi.leafy.place.dto.common.Response;
import lombok.Data;

@Data
public class RuralApiResponse<T> {
    private Response<T> response;
}
