package com.chocobi.leafy.place.dto.farm;

import com.chocobi.leafy.place.dto.common.Header;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmListApiResponse {
    private Header header;
    private FarmListBody body;
}
