package com.chocobi.leafy.place.fetcher.farm.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmListBody {
    @XmlElement(name = "items")
    private FarmListItems farmListItems;
}
