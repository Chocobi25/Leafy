package com.chocobi.leafy.external.farm.dto;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmRestaurantDetailResponse {
    private ExternalApiResponse.Header header;
    private Body body;

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        @XmlElement(name = "item")
        private FarmRestaurantDetailItem item;
    }

    @Getter
    public static class FarmRestaurantDetailItem {
        private String cntntsNo;
        private String cntntsSj;
        private String locplc;
        private String telno;
        private String url;
        private String smm;
    }
}
