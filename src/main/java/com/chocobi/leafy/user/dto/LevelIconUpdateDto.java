package com.chocobi.leafy.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelIconUpdateDto {

    @NotBlank(message = "선택한 아이콘은 필수입니다.")
    private String selectedLevelIcon;
}