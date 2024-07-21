package com.floweytf.fma.mixin.umm;

import ch.njol.minecraft.uiframework.hud.HudElement;
import ch.njol.unofficialmonumentamod.features.effects.Effect;
import ch.njol.unofficialmonumentamod.features.effects.EffectOverlay;
import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.FormatUtil;
import static com.floweytf.fma.util.FormatUtil.join;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = EffectOverlay.class,
    remap = false
)
public abstract class EffectOverlayMixin extends HudElement {
    @Shadow @Final private ArrayList<Effect> effects;
    @Unique
    private static final Set<MobEffect> BAD_EFFECTS = Set.of(
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.DIG_SLOWDOWN,
        MobEffects.CONFUSION,
        MobEffects.BLINDNESS,
        MobEffects.HUNGER,
        MobEffects.WEAKNESS,
        MobEffects.POISON,
        MobEffects.WITHER,
        MobEffects.UNLUCK,
        MobEffects.BAD_OMEN
    );

    @Unique
    private void fma$renderLine(Font font, PoseStack matrix, Component text, boolean rAlign, int width,
                                LocalIntRef currentY) {
        font.drawShadow(matrix, text, rAlign ? (float) (width - 5 - font.width(text)) : 5.0F, currentY.get(), -1);
        currentY.set(currentY.get() + 11);
    }

    // we need to render vanilla effects as well...
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;"
        )
    )
    private void renderVanilla(
        PoseStack matrices, float tickDelta, CallbackInfo ci,
        @Local(name = "currentY") LocalIntRef currentY,
        @Local boolean rAlign,
        @Local Font font,
        @Local(name = "width") int width
    ) {
        if (!FMAClient.features().enableVanillaEffectInUMMHud) {
            return;
        }

        final var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final var space = Component.literal(" ");

        fma$renderLine(font, matrices, Component.translatable("hud.fma.ummEffects.vanillaCategory"), rAlign, width, currentY);

        for (final var activeEffect : player.getActiveEffects()) {
            final var nameText = activeEffect.getEffect().getDisplayName();
            final var amp = activeEffect.getAmplifier();
            final var levelText = Component.literal(amp == 0 ? "" : amp + " ");
            final var timeText = MobEffectUtil.formatDuration(activeEffect, 1f);
            final var color = BAD_EFFECTS.contains(activeEffect.getEffect()) ? ChatFormatting.RED :
                ChatFormatting.GREEN;

            final var effectText = join(nameText, space, levelText).withStyle(color);

            final var text = rAlign ? join(effectText, timeText) : join(timeText, space, effectText);

            fma$renderLine(font, matrices, text, rAlign, width, currentY);
        }

        if (!effects.isEmpty()) {
            fma$renderLine(font, matrices, Component.translatable("hud.fma.ummEffects.monumentaCategory"), rAlign, width, currentY);
        }
    }

    @ModifyReturnValue(
        method = "getHeight",
        at = @At("RETURN")
    )
    private int modifyHeight(int original) {
        if (!FMAClient.features().enableVanillaEffectInUMMHud) {
            return original;
        }

        final var player = Minecraft.getInstance().player;

        if (player == null) {
            return original;
        }

        return original + 11 * (effects.isEmpty() ? 1 : 2 + player.getActiveEffects().size());
    }

    @ModifyReturnValue(
        method = "isVisible",
        at = @At("RETURN")
    )
    private boolean setVisibleVanillaEffects(boolean original) {
        if (!FMAClient.features().enableVanillaEffectInUMMHud) {
            return original;
        }

        final var player = Minecraft.getInstance().player;

        if (player == null) {
            return original;
        }

        return original || !player.getActiveEffects().isEmpty();
    }
}
