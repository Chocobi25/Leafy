package com.chocobi.leafy.place.dto.eco;

import lombok.Data;

@Data
public class EcoItem {
    private String addr;            //주소
    private String mainimage;       //썸네일
    private String title;           //제목
    private String summary;         //설명
    private String tel;             //전화번호
}
