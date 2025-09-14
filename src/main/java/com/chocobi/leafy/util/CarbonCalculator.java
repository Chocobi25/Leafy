package com.chocobi.leafy.util;

import com.chocobi.leafy.constants.CarbonEmissionConst;

public class CarbonCalculator {

    public static double CalculateCarCarbonEmission(double distance) {
        return distance / 1000.0 * CarbonEmissionConst.CAR_EMISSION;
    }

    public static double CalculatePublicTransCarbonEmission(double subwayDistance, double trainDistance, double busDistance ){
        return (subwayDistance / 1000.0) * CarbonEmissionConst.SUBWAY_EMISSION + (trainDistance / 1000.0) * CarbonEmissionConst.TRAIN_EMISSION + (busDistance / 1000.0) * CarbonEmissionConst.BUS_EMISSION;
    }

    public static double CalculateFerryCarbonEmission(double ferryDistance) {
        return (ferryDistance / 1000.0) * CarbonEmissionConst.FERRY_EMISSION;
    }
}
