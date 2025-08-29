package com.chocobi.leafy.distance.dto;

import lombok.Data;

import java.util.List;

@Data
public class KakaoNaviResponse {
    private String trans_id;
    private List<Routes> routes;
}
