package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
        method = "displayScoreboardSidebar",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderCustomSidebar(PoseStack poseStack, Objective objective, CallbackInfo ci) {
        if (FMAClient.features().sidebarToggles.enable) {
            ci.cancel();
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V",
            ordinal = 0
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/Gui;displayScoreboardSidebar" +
                    "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/scores/Objective;)V"
            )
        )
    )
    private void renderCustomSidebar(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (FMAClient.features().sidebarToggles.enable) {
            FMAClient.SIDEBAR.render(minecraft, poseStack);
        }
    }

    @WrapWithCondition(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;)V"
        )
    )
    private boolean wrapEffectHudPredicate(Gui instance, PoseStack poseStack) {
        return !FMAClient.features().enableVanillaEffectInUMMHud;
    }
}
