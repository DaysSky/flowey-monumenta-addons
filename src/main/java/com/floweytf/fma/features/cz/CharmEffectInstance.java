package com.floweytf.fma.features.cz;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.FMAConfig;
import com.floweytf.fma.features.cz.data.CharmEffectRarity;
import com.floweytf.fma.features.cz.data.CharmEffectType;
import com.floweytf.fma.util.FormatUtil;
import static com.floweytf.fma.util.FormatUtil.join;
import static com.floweytf.fma.util.FormatUtil.tabulate;
import com.floweytf.fma.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class CharmEffectInstance {
    private final double rollValue;
    private final CharmEffectType effect;
    private final CharmEffectRarity rarity;

    private final double baseValue;
    private final double delta;
    private final double value;
    private final Style rarityColor;
    private final Style rollColor;

    public CharmEffectInstance(double rollValue, CharmEffectType effect, CharmEffectRarity rarity) {
        this.rollValue = rollValue;
        this.effect = effect;
        this.rarity = rarity;
        // we should compute a few other values
        final var rawBaseValue = effect.rarityValue(rarity.rarity);

        var baseValue = rawBaseValue;
        var delta = effect.variance * (2 * rollValue - 1);
        double value = baseValue;

        if (effect.variance != 0) {
            value = baseValue + delta;
            if (baseValue >= 5) {
                value = Math.round(value);
            } else {
                value = twoDecimal(value);
            }
        }

        if (rarity.isNegative) {
            value = -value;
            baseValue = -baseValue;
            delta = -delta;
        }

        // prep colors
        rarityColor = Style.EMPTY.withColor(rarity.color);

        // basically, if the base value is negative, we can assume that a low roll is good
        // or else we can assume a high roll is good
        // if it's negative, we negate this

        final var rollValueForColor = (rawBaseValue < 0 == rarity.isNegative) ? rollValue :
            (1 - rollValue);

        rollColor = Style.EMPTY.withColor(Util.colorRange((float) rollValueForColor));
        this.baseValue = baseValue;
        this.delta = delta;
        this.value = value;
    }

    private String unit() {
        return effect.isPercent ? "%" : "";
    }

    private String fmt(double value) {
        return FormatUtil.fmtDouble(value) + unit();
    }

    private double twoDecimal(double value) {
        return Math.round(value * 100) / 100.0;
    }

    private Component effectText() {
        return Component.literal(effect.modifier).withStyle(rarityColor);
    }

    private Component modText() {
        return Component.literal(fmt(value)).withStyle(rarityColor);
    }

    private Component modDetailText() {
        return join(
            Component.literal(fmt(baseValue)).withStyle(rarityColor),
            effect.variance == 0 ?
                Component.literal("+0" + unit()).withStyle(ChatFormatting.DARK_GRAY) :
                Component.literal(fmt(twoDecimal(delta))).withStyle(rollColor)
        );
    }

    private Component rarityText() {
        return Component.literal(rarity.getShorthand()).withStyle(rarityColor);
    }

    private Component rollText() {
        return Component.literal(FormatUtil.fmtDouble(twoDecimal(rollValue * 100), false) + "%").withStyle(rollColor);
    }

    private List<Component> formatParts(FMAConfig.Zenith config, boolean includeCharmEffect) {
        final var parts = new ArrayList<Component>();

        parts.add(modText());

        if (includeCharmEffect) {
            parts.add(effect.ability.coloredName);
        }

        parts.add(effectText());

        if (config.enableStatBreakdown)
            parts.add(modDetailText());
        if (config.displayEffectRarity)
            parts.add(rarityText());
        if (config.displayRollValue)
            parts.add(rollText());

        return parts;
    }

    private static List<Component> getHeader(FMAConfig.Zenith config, boolean includeCharmAbility) {
        final var header = new ArrayList<Component>();
        header.add(Component.literal("Mod").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));
        if (includeCharmAbility) {
            header.add(Component.literal("Ability").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));
        }

        header.add(Component.literal("Effect").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));

        if (config.enableStatBreakdown)
            header.add(Component.literal("Mod Detail").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));
        if (config.displayEffectRarity)
            header.add(Component.literal("Rarity").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));
        if (config.displayRollValue)
            header.add(Component.literal("Roll").withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));
        return header;
    }

    public static List<Component> format(Font font, List<CharmEffectInstance> entries, boolean includeCharmEffect) {
        final var config = FMAClient.config().zenith;

        if (config.peliCompatibilityMode) {
            return entries.stream().map(entry -> (Component) join(
                entry.modText(),
                Component.literal(" "),
                entry.effect().ability.coloredName.copy().withStyle(entry.modText().getStyle()),
                Component.literal(" "),
                entry.effectText(),
                Component.literal(" ["),
                entry.rollText(),
                Component.literal("]")
            )).toList();
        } else {
            return tabulate(font, Stream.concat(
                Stream.of(getHeader(config, includeCharmEffect)),
                entries.stream().map(entry -> entry.formatParts(config, includeCharmEffect))
            ).toList());
        }
    }

    public CharmEffectType effect() {
        return effect;
    }
}
