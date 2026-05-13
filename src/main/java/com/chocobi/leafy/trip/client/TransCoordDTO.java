package com.chocobi.leafy.trip.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransCoordDTO {
    @NotNull
    @Positive
    private Long tripId;

    @NotBlank
    @Size(max = 30)
    private String x;

    @NotBlank
    @Size(max = 30)
    private String y;
}
