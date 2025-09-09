package com.chocobi.leafy.distance.dto;

import lombok.Data;

import java.util.List;

@Data
public class Routes {
    private Summary summary;
    private Integer result_code;
    private String result_message;
    private List<Section> sections;
}
