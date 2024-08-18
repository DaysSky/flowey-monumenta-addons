package com.floweytf.fma.features.cz;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.features.cz.data.CharmRarity;
import com.floweytf.fma.features.cz.data.ZenithAbility;
import com.floweytf.fma.features.cz.data.ZenithClass;
import static com.floweytf.fma.util.FormatUtil.join;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public final class Charm {
    private final int charmPower;
    private final long uuid; // It's not a real UUID, but that's what monumenta calls it
    private final CharmRarity rarity;
    private final List<CharmEffectInstance> effects;
    private final List<ZenithClass> classes;
    private final List<ZenithAbility> abilities;
    private final int budget;
    private final boolean hasUpgraded;

    public Charm(int charmPower, long uuid, CharmRarity rarity, List<CharmEffectInstance> effects, int budget,
                 boolean hasUpgraded) {
        this.charmPower = charmPower;
        this.uuid = uuid;
        this.rarity = rarity;
        this.effects = new ArrayList<>(effects);
        this.hasUpgraded = hasUpgraded;
        this.effects.sort(Comparator.comparingInt(x -> x.effect().ordinal()));

        classes = effects.stream()
            .map(x -> x.effect().ability.zenithClass)
            .collect(Collectors.toSet())
            .stream()
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toList();

        abilities = effects.stream()
            .map(x -> x.effect().ability)
            .collect(Collectors.toSet())
            .stream()
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toList();

        this.budget = budget;
    }

    private void buildSingleAbilityCharm(Font font, List<Component> target) {
        target.add(join(
            Component.literal("Ability").withStyle(ChatFormatting.GRAY),
            Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY),
            abilities.get(0).coloredName,
            Component.literal(" (").withStyle(ChatFormatting.GRAY),
            classes.get(0).coloredName,
            Component.literal(")").withStyle(ChatFormatting.GRAY)
        ));

        target.add(Component.empty());
        target.addAll(CharmEffectInstance.format(font, effects, false));
    }

    private void buildSingleClassCharm(Font font, List<Component> target) {
        target.add(join(
            Component.literal("Treelocked").withStyle(ChatFormatting.GRAY),
            Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY),
            classes.get(0).coloredName
        ));

        target.add(Component.empty());
        target.addAll(CharmEffectInstance.format(font, effects, true));
    }

    private void buildMulticlassCharm(Font font, List<Component> target) {
        var classText = Component.empty();

        for (int i = 0; i < classes.size() - 1; i++) {
            classText = classText.append(classes.get(i).coloredShortName)
                .append(Component.literal(",").withStyle(ChatFormatting.DARK_GRAY));
        }
        classText.append(classes.get(classes.size() - 1).coloredShortName);

        target.add(join(
            Component.literal("Multi-tree").withStyle(ChatFormatting.GRAY),
            Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY),
            classText
        ));

        target.add(Component.empty());
        target.addAll(CharmEffectInstance.format(font, effects, true));
    }

    public void buildLore(Font font, List<Component> target) {
        target.add(join(
            Component.literal("Architect's Ring : ").withStyle(ChatFormatting.DARK_GRAY),
            Component.literal("Zenith Charm").withStyle(style -> style.withColor(0xFF9CF0))
        ));

        final var rarityText = hasUpgraded ? rarity.coloredText.copy().append(" (u)") : rarity.coloredText;

        target.add(join(
            Component.literal("Charm Power : ").withStyle(ChatFormatting.DARK_GRAY),
            Component.literal("★".repeat(charmPower)).withStyle(style -> style.withColor(0xFFFA75)),
            Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY),
            rarityText,
            Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY),
            Component.literal("Budget : " + budget).withStyle(ChatFormatting.GRAY)
        ));

        if (FMAClient.config().zenith.displayUUID) {
            target.add(join(
                Component.literal("UUID : ").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(Long.toHexString(uuid).toUpperCase()).withStyle(ChatFormatting.GREEN)
            ));
        }

        // We need to guess the charm type
        // There are 3 types:
        // 0. Single ability
        // 1. Single class
        // 2. Multi class

        // Formats:
        // 0. Ability - ability (class)
        // 1. Class - class
        // ability
        //   - thing
        //   - thing
        // 2. Multiclass - class list
        // Class - ability
        //   - thing
        //   - thing

        if (classes.isEmpty() || abilities.isEmpty()) {
            throw new IllegalStateException("wtf? empty charm?");
        }

        if (classes.size() == 1) {
            if (abilities.size() == 1) {
                buildSingleAbilityCharm(font, target);
            } else {
                buildSingleClassCharm(font, target);
            }
        } else {
            buildMulticlassCharm(font, target);
        }
    }
}
