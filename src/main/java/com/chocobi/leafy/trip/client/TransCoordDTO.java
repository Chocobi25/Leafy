package com.chocobi.leafy.trip.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransCoordDTO {

    @NotBlank
    @Size(max = 30)
    @Pattern(
            regexp = "^[+-]?((180(\\.0{1,15})?)|((1[0-7]\\d|[1-9]?\\d)(\\.\\d{1,15})?))$",
            message = "x 좌표 형식이 올바르지 않습니다."
    )
    private String x;

    @NotBlank
    @Size(max = 30)
    @Pattern(
            regexp = "^[+-]?((90(\\.0{1,15})?)|(([1-8]?\\d)(\\.\\d{1,15})?))$",
            message = "y 좌표 형식이 올바르지 않습니다."
    )
    private String y;
}
