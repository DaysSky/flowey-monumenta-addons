package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.ItemUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(
        method = "renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;" +
            "Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isBarVisible()Z"
        )
    )
    private void renderVanityDurability(PoseStack poseStack, Font font, ItemStack stack, int x, int y, String text,
                                        CallbackInfo ci) {
        final var res = ItemUtil.getVanityDurabilityInfo(stack);
        if (res.isEmpty() || !FMAClient.features().enableVanityDurability) {
            return;
        }

        final var data = res.get();
        final var durability = data.firstInt();
        final var maxDurability = data.firstInt();

        RenderSystem.disableDepthTest();
        int width = Math.round((durability * 13.0f) / data.secondInt());
        int color = Mth.hsvToRgb(
            durability / (maxDurability * 3.0f),
            1.0f,
            1.0f
        );
        int barX = x + 2;
        int barY = y + 13;
        GuiComponent.fill(poseStack, barX, barY, barX + 13, barY + 2, -16777216);
        GuiComponent.fill(poseStack, barX, barY, barX + width, barY + 1, color | 0xFF000000);
        RenderSystem.enableDepthTest();
    }
}
