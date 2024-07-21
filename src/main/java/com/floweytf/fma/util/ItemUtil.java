package com.floweytf.fma.util;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemUtil {
    public static Optional<IntIntPair> getVanityDurabilityInfo(ItemStack stack) {
        // Items with durability don't have custom vanity info
        if (stack.getItem().canBeDepleted()) {
            return Optional.empty();
        }

        final var tag = stack.getTag();

        if (tag == null) {
            return Optional.empty();
        }

        if (!tag.contains("display", 10)) {
            return Optional.empty();
        }

        final var displayTag = tag.getCompound("display");

        if (displayTag.getTagType("Lore") != 9) {
            return Optional.empty();
        }

        final var loreList = displayTag.getList("Lore", 8);

        if (loreList.size() < 2) {
            return Optional.empty();
        }

        final var lastLine = Component.Serializer.fromJson(loreList.getString(loreList.size() - 1));

        if (lastLine == null) {
            return Optional.empty();
        }

        final var lastLineRaw = lastLine.getString();

        if (!lastLineRaw.startsWith("Durability: ")) {
            return Optional.empty();
        }

        final var parts = lastLineRaw.split("\\s+");
        if (parts.length != 4) {
            return Optional.empty();
        }

        return Optional.of(IntIntPair.of(
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[3])
        ));
    }
}