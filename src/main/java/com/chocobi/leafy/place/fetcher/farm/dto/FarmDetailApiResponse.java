package com.chocobi.leafy.place.fetcher.farm.dto;

import com.chocobi.leafy.place.common.dto.Header;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmDetailApiResponse {
    private Header header;
    private FarmDetailBody body;
}
