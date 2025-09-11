package com.chocobi.leafy.constants;

import com.chocobi.leafy.distance.domain.Point;
import com.chocobi.leafy.distance.domain.Port;

import java.util.List;

public class PortConst {
    /**
     * 제주도로 출발하는 육지 항구
     */
    public static final List<Port> JEJU_DEPART_PORTS = List.of(
            new Port("목포항 여객터미널", new Point(126.3910, 34.7930)),
            new Port("완도항 여객선터미널", new Point(126.7550, 34.3110)),
            new Port("여수항 여객선터미널", new Point(127.7650, 34.7390)),
            new Port("고흥 녹동항 여객터미널", new Point(127.5250, 34.4500)),
            new Port("삼천포항 여객터미널", new Point(128.0570, 34.9310)),
            new Port("진도항 여객선터미널", new Point(126.2620, 34.3120))
    );

    /**
     * 제주도에 도착하는 주요 항구들
     */
    public static final List<Port> JEJU_ARRIVE_PORTS = List.of(
            new Port("제주항 연안여객터미널", new Point(126.5180, 33.5110))
    );

    public static boolean isFerrySection(Point startPoint, Point endPoint) {
        for (Port departPort : PortConst.JEJU_DEPART_PORTS) {
            for (Port arrivePort : PortConst.JEJU_ARRIVE_PORTS) {
                // (출발점=육지, 도착점=제주)
                boolean forward = startPoint.equals(departPort.getPoint()) && endPoint.equals(arrivePort.getPoint());
                // (출발점=제주, 도착점=육지)
                boolean backward = startPoint.equals(arrivePort.getPoint()) && endPoint.equals(departPort.getPoint());

                if (forward || backward) {
                    return true;
                }
            }
        }
        return false;
    }
}
