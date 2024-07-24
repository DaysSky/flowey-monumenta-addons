package com.floweytf.fma.features.cz;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.FMAConfig;
import com.floweytf.fma.features.cz.data.CharmEffectRarity;
import com.floweytf.fma.features.cz.data.CharmEffectType;
import com.floweytf.fma.features.cz.data.CharmRarity;
import com.floweytf.fma.util.NBTUtil;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CharmItemManager {
    public static final String ZENITH_CHARM_TIER = "zenithcharm";

    public static final String CHARM_EFFECTS_KEY = "DEPTHS_CHARM_EFFECT";
    public static final String CHARM_ACTIONS_KEY = "DEPTHS_CHARM_ACTIONS";
    public static final String CHARM_ROLLS_KEY = "DEPTHS_CHARM_ROLLS";

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if(!FMAClient.config().zenith.enableCustomCharmInfo) {
                return;
            }

            try {
                getCharm(stack).ifPresent(charm -> {
                    if (FMAClient.config().zenith.disableMonumentaLore && !FMAClient.features().enableDebug) {
                        lines.subList(1, lines.size()).clear();
                    } else {
                        lines.add(Component.empty());
                        lines.add(Component.literal("- Custom tooltip -"));
                        lines.add(Component.empty());
                    }

                    charm.buildLore(Minecraft.getInstance().fontFilterFishy, lines);
                });
            } catch (Exception e) {
                lines.add(Component.literal("* Failed to parse charm data *").withStyle(ChatFormatting.RED));
                lines.add(Component.literal("* See logs *").withStyle(ChatFormatting.RED));
                FMAClient.LOGGER.error("uh oh", e);
            }
        });
    }

    private static Supplier<IllegalStateException> ise(String text) {
        return () -> new IllegalStateException(text);
    }

    public static Optional<Charm> getCharm(ItemStack item) {
        // TODO: this probably won't port well to 1.20.5, but that's too far in the future... Perhaps I'm
        //  foot-gunning myself...
        final var monumenta = NBTUtil.getMonumenta(item);
        final var tier = NBTUtil.getTier(item);
        final var charmPower = NBTUtil.getCharmPower(item);

        if (tier.isEmpty() || charmPower.isEmpty() || !tier.get().equals(ZENITH_CHARM_TIER) || monumenta.isEmpty()) {
            return Optional.empty();
        }

        // meow meow meow meow meow meow
        final var charmDataOptional = NBTUtil.getCompound(monumenta.get(), "PlayerModified");
        if (charmDataOptional.isEmpty()) {
            return Optional.empty();
        }

        final var charmData = charmDataOptional.get();

        final var uuid = charmData.getLong("DEPTHS_CHARM_UUID");
        final var rarity = CharmRarity.values()[charmData.getInt("DEPTHS_CHARM_RARITY") - 1];

        final var effectsCount = charmData.getAllKeys().stream()
            .filter(x -> x.startsWith(CHARM_EFFECTS_KEY))
            .map(x -> x.substring(CHARM_EFFECTS_KEY.length()))
            .map(Integer::parseInt)
            .reduce(Integer::max);

        if (effectsCount.isEmpty()) {
            throw new IllegalStateException("unable to obtain charm effect list");
        }

        final var charmBudget = new int[]{0};

        final var effects = IntStream.range(1, effectsCount.get() + 1).mapToObj(i -> {
            final var roll = NBTUtil.getDouble(charmData, CHARM_ROLLS_KEY + i)
                .orElseThrow(ise("CHARM_ROLLS_KEY must be double"));

            final var effectKey = NBTUtil.getString(charmData, CHARM_EFFECTS_KEY + i)
                .orElseThrow(ise("CHARM_EFFECTS_KEY can't be found"));

            final var effectType = CharmEffectType.byName(effectKey)
                .orElseThrow(ise("CHARM_EFFECTS_KEY can't be matched: " + effectKey));

            final var effectRarityOpt = NBTUtil.getString(charmData, CHARM_ACTIONS_KEY + (i - 1))
                .map(x -> CharmEffectRarity.byName(x).orElseThrow(ise("CHARM_ACTIONS_KEY can't be found")));

            final var effectRarity = effectRarityOpt
                .orElse(CharmEffectRarity.values()[rarity.ordinal()]);

            effectRarityOpt.ifPresent(r -> charmBudget[0] -= r.budget);
            return new CharmEffectInstance(roll, effectType, effectRarity);
        }).toList();

        return Optional.of(new Charm(charmPower.get(), uuid, rarity, effects, charmBudget[0]));
    }
}
