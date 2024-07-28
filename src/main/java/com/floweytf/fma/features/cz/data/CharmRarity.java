package com.floweytf.fma.features.cz.data;

import net.minecraft.network.chat.Component;

public enum CharmRarity {
    COMMON("Common", 0x9f929c),
    UNCOMMON("Uncommon", 0x70bc6d),
    RARE("Rare", 0x705eca),
    EPIC("Epic", 0xcd5eca),
    LEGENDARY("Legendary", 0xe49b20);

    public final String displayName;
    public final int color;
    public final Component coloredText;

    CharmRarity(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
        this.coloredText = Component.literal(displayName).withStyle(s -> s.withColor(color));
    }
}
