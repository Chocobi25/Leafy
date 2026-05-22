package com.chocobi.leafy.external.common.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import lombok.Getter;

@Getter
public class ExternalApiResponse<T> {
    private Header header;
    private Body<T> body;

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    public static class Body<T> {
        private int numOfRows;
        private int pageNo;
        private int totalCount;
        private Items<T> items;
    }

    @Getter
    public static class Items<T> {
        private List<T> item;
    }
}