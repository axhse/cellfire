package com.example.cellfire.data;

public final class ForestTypeConditions {
    public static double determineActivationEnergy(int forestType) {
        return switch (forestType) {
            case 1 -> 120_000;
            case 2 -> 140_000;
            case 3 -> 160_000;
            case 4 -> 170_000;
            case 5 -> 150_000;
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
