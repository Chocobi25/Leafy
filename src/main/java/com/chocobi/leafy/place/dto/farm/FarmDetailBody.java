package com.chocobi.leafy.place.dto.farm;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmDetailBody {
    @XmlElement(name = "items")
    private FarmListItems farmListItems;
}
