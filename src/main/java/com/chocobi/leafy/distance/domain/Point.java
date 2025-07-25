package com.chocobi.leafy.distance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // getter/setter, toString(), equals(), hashCode() 등 자동 생성
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    private Double x;
    private Double y;
}
