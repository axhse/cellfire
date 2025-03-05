package com.example.cellfire.data;

public final class ForestTypeConditions {
    public static double determineIgnitionTemperature(int forestType) {
        return switch (forestType) {
            case 1 -> 325;
            case 2 -> 275;
            case 3 -> 305;
            case 4 -> 235;
            case 5 -> 285;
            default -> Double.POSITIVE_INFINITY;
        };
    }

    public static double determineActivationEnergy(int forestType) {
        return switch (forestType) {
            case 1 -> 125_000;
            case 2 -> 105_000;
            case 3 -> 115_000;
            case 4 -> 95_000;
            case 5 -> 110_000;
            default -> Double.POSITIVE_INFINITY;
        };
    }

    public static final class ForestType {
        public static final int TREELESS  = 0;
        public static final int EVERGREEN_NEEDLE_LEAF = 1;
        public static final int EVERGREEN_BROADLEAF = 2;
        public static final int DECIDUOUS_NEEDLE_LEAF = 3;
        public static final int DECIDUOUS_BROADLEAF = 4;
        public static final int MIXED = 5;
    }
}
