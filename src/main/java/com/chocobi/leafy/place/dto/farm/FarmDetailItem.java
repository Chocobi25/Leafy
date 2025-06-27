package com.chocobi.leafy.place.dto.farm;

import lombok.Data;

@Data
public class FarmDetailItem {
    private String cntntsNo;    //콘텐츠 번호
    private String cntntsSj;    //제목
    private String locplc;      //주소
    private String telno;       //전화번호
    private String url;         //홈페이지
    private String smm;         //내용
}
