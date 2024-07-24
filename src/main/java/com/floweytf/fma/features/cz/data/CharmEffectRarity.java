package com.floweytf.fma.features.cz.data;

import java.util.Arrays;
import java.util.Optional;

public enum CharmEffectRarity {
    COMMON("Common", CharmRarity.COMMON, -5, false),
    UNCOMMON("Uncommon", CharmRarity.UNCOMMON, -8, false),
    RARE("Rare", CharmRarity.RARE, -11, false),
    EPIC("Epic", CharmRarity.EPIC, -14, false),
    LEGENDARY("Legendary", CharmRarity.LEGENDARY, -16, false),
    N_COMMON("Negative Common", CharmRarity.COMMON, 4, true, 0xffc8d2),
    N_UNCOMMON("Negative Uncommon", CharmRarity.UNCOMMON, 7, true, 0xffc8d2),
    N_RARE("Negative Rare", CharmRarity.RARE, 10, true, 0xf35754),
    N_EPIC("Negative Epic", CharmRarity.EPIC, 13, true, 0xe73730),
    N_LEGENDARY("Negative Legendary", CharmRarity.LEGENDARY, 15, true, 0xd70000);

    public final String name;
    public final CharmRarity rarity;
    public final int budget;
    public final boolean isNegative;
    public final int color;

    CharmEffectRarity(String name, CharmRarity rarity, int budget, boolean isNegative) {
        this(name, rarity, budget, isNegative, rarity.color);
    }

    CharmEffectRarity(String name, CharmRarity rarity, int budget, boolean isNegative, int color) {
        this.name = name;
        this.rarity = rarity;
        this.budget = budget;
        this.isNegative = isNegative;
        this.color = color;
    }

    public static Optional<CharmEffectRarity> byName(String actionName) {
        return Arrays.stream(values())
            .filter(x -> actionName.equalsIgnoreCase(x.name))
            .findFirst();
    }

    public String getShorthand() {
        return (isNegative ? "-" : "") + rarity.displayName;
    }
}
