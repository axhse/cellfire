package com.example.cellfire.data;

public final class ForestConditions {
    public static double determineIgnitionTemperature(byte forestType) {
        return switch (forestType) {
            case 1 -> 325;
            case 2 -> 275;
            case 3 -> 305;
            case 4 -> 235;
            case 5 -> 285;
            default -> 10_000;
        };
    }

    public static double determineActivationEnergy(byte forestType) {
        return switch (forestType) {
            case 1 -> 125_000;
            case 2 -> 105_000;
            case 3 -> 115_000;
            case 4 -> 95_000;
            case 5 -> 110_000;
            default -> 10_000_000;
        };
    }

    public static final class ForestType {
        public static final byte TREELESS  = 0;
        public static final byte EVERGREEN_NEEDLE_LEAF = 1;
        public static final byte EVERGREEN_BROADLEAF = 2;
        public static final byte DECIDUOUS_NEEDLE_LEAF = 3;
        public static final byte DECIDUOUS_BROADLEAF = 4;
        public static final byte MIXED = 5;
    }
}
