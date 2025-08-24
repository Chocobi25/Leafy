package com.chocobi.leafy.place.fetcher.image.dto;

import com.chocobi.leafy.place.common.dto.Response;
import lombok.Data;

@Data
public class ImageApiResponse<ImageItem> {
    Response<ImageItem> response;
}
