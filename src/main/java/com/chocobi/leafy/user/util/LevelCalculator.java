package com.chocobi.leafy.user.util;

import com.chocobi.leafy.user.Entity.Level;

public class LevelCalculator {

    private static final double LV2_THRESHOLD = 5.0;
    private static final double LV3_THRESHOLD = 20.0;
    private static final double LV4_THRESHOLD = 50.0;
    private static final double LV5_THRESHOLD = 100.0;

    public static Level calculateLevel(double totalCarbonSaved) {
        if (totalCarbonSaved >= LV5_THRESHOLD) {
            return Level.LV5;
        }
        if (totalCarbonSaved >= LV4_THRESHOLD) {
            return Level.LV4;
        }
        if (totalCarbonSaved >= LV3_THRESHOLD) {
            return Level.LV3;
        }
        if (totalCarbonSaved >= LV2_THRESHOLD) {
            return Level.LV2;
        }
        return Level.LV1;
    }

}
