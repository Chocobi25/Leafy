package com.chocobi.leafy.external.farm.dto;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.Getter;

@Getter
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmRestaurantListResponse {
    private ExternalApiResponse.Header header;
    private Body body;

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        @XmlElement(name = "items")
        private Items items;
    }

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Items {
        @XmlElement(name = "item")
        private List<FarmRestaurantListItem> item;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Getter
    public static class FarmRestaurantListItem {
        private String cntntsNo;
        private String cntntsSj;
        private String thumbImgUrl;
    }
}
