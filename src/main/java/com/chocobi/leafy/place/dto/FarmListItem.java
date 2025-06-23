package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class FarmListItem {
    private String cntntsNo;    //콘텐츠 번호
    private String cntntsSj;    //명칭
    private String adstrdNm;    //지역
    private String telno;       //전화번호
    private String restde;      //쉬는 날
    private String bsnTime;     //영업 시간
    private String thumbImgUrl; //썸네일
}
