package com.chocobi.leafy.place.common.dto;

import lombok.Data;

@Data
public class Response<T> {
    private Header header;
    private Body<T> body;
}
