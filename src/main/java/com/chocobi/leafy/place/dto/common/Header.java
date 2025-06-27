package com.chocobi.leafy.place.dto.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Header {
    private String resultCode;
    private String resultMsg;
}
