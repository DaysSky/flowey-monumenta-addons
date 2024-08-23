package com.floweytf.fma.features.cz;

public enum CharmType {
    ABILITY(1),
    TREE(1.35),
    WILDCARD(1.8);

    private final double factor;

    CharmType(double factor) {
        this.factor = factor;
    }

    public double factor() {
        return factor;
    }

    public static CharmType byId(int typeId) {
        if (typeId < 4) {
            return ABILITY;
        }

        if (typeId < 9) {
            return TREE;
        }

        return WILDCARD;
    }
}
