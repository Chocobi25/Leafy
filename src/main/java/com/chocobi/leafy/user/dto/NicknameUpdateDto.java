package com.chocobi.leafy.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NicknameUpdateDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 12, message = "닉네임은 12자 이하여야 합니다.")
    private String nickname;
}