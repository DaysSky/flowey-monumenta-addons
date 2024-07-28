package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.Graphics;
import com.floweytf.fma.features.cz.CharmItemManager;
import com.floweytf.fma.util.NBTUtil;
import com.floweytf.fma.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
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
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"
        )
    )
    private void renderItemOverlay(PoseStack poseStack, Font font, ItemStack stack, int x, int y, String text,
                                   CallbackInfo ci) {
        try {
            final var config = FMAClient.features().inventoryOverlay;

            if (!config.enable) {
                return;
            }

            final var tier = NBTUtil.getTier(stack);
            final var czCharmData = NBTUtil.getMonumenta(stack)
                .flatMap(c -> NBTUtil.getCompound(c, CharmItemManager.CHARM_KEY));
            final var charmPower = NBTUtil.getCharmPower(stack);

            if (config.enableRarity) {
                tier.flatMap(t -> Util.get(Graphics.RARITY_TO_TEXTURE, t))
                    .ifPresent(texture -> Graphics.renderTexture(poseStack, x, y, 0, 0, 16, 16, 16, 16, texture));
            }

            if (config.enableCZCharmRarity) {
                czCharmData.flatMap(c -> NBTUtil.getInt(c, CharmItemManager.CHARM_RARITY_KEY))
                    .map(c -> Graphics.CHARM_RARITY_TO_TEXTURE.get(c - 1))
                    .ifPresent(texture -> Graphics.renderTexture(poseStack, x, y, 0, 0, 16, 16, 16, 16, texture));
            }

            if (config.enableCooldown && charmPower.isEmpty()) {
                final var cooldowns = FMAClient.player().getCooldowns();
                if (cooldowns.cooldowns.containsKey(stack.getItem())) {
                    final var endTime = cooldowns.cooldowns.get(stack.getItem()).endTime;
                    final var startTime = cooldowns.cooldowns.get(stack.getItem()).startTime;
                    final var currTime = cooldowns.tickCount;
                    final var timeLeft = (endTime - currTime + 19) / 20;
                    Graphics.drawString(
                        poseStack, font, Component.literal(String.valueOf(timeLeft)), x, y, 200,
                        0xff000000 | Util.colorRange(currTime - startTime, endTime - startTime)
                    );
                }
            }

            if (config.enableCZCharmPower && charmPower.isPresent()) {
                poseStack.pushPose();
                poseStack.translate(x, y, 200);
                poseStack.scale(0.5f, 0.5f, 1);

                final var cp = charmPower.get();
                final var str = cp < 4 ? "★".repeat(cp) : cp + "★";

                czCharmData.ifPresent(compoundTag -> Graphics.drawString(
                    poseStack, font,
                    Component.literal(str),
                    0, 0, 0xFFFFFA75
                ));
                poseStack.popPose();
            }

            final var itemName = stack.getDisplayName().getString();

            if (config.enablePICount && (itemName.contains("Potion Injector") || itemName.contains("Iridium Injector"))) {
                final var countOpt = NBTUtil.getLore(stack).flatMap(lore -> {
                    boolean f = false;
                    for (final var tag : lore) {
                        final var str = NBTUtil.jsonToRaw(tag.getAsString());

                        if (f) {
                            return Optional.of(str);
                        }
                        if (str.contains("Selected Potion")) {
                            f = true;
                        }
                    }

                    return Optional.empty();
                }).flatMap(potionName -> NBTUtil.getInventory(stack)
                    .map(inventory -> inventory.stream()
                        .filter(item -> NBTUtil.getName(item).map(NBTUtil::jsonToRaw).map(potionName::equals).orElse(false))
                        .map(ItemStack::getCount)
                        .reduce(0, Integer::sum)
                    )
                );

                countOpt.ifPresent(count -> {
                    final var t = Component.literal(String.valueOf(count));

                    Graphics.drawString(
                        poseStack, font, t,
                        x + 17 - font.width(t), y + 9, 200, 0xff000000 | Util.colorRange(count, 27)
                    );
                });
            }

            if (config.enableLoomFirmCount && (itemName.contains("Doorway from Eternity") || itemName.contains(
                "Worldshaper's Loom")) || itemName.contains("Firmament")) {
                final var countOpt = NBTUtil.getInventory(stack)
                    .map(inventory -> inventory.stream()
                        .map(ItemStack::getCount)
                        .reduce(0, Integer::sum)
                    );

                countOpt.ifPresent(count -> {
                    final var t = Component.literal(String.valueOf(count));

                    if (count < 100) {
                        Graphics.drawString(
                            poseStack, font, t,
                            x + 17 - font.width(t), y + 9, 200, 0xffffffff
                        );
                    } else {
                        poseStack.pushPose();
                        poseStack.translate(x + 17 - (float) font.width(t) / 2, y + 12, 200);
                        poseStack.scale(0.5f, 0.5f, 1);

                        Graphics.drawString(
                            poseStack, font, t,
                            0, 0, 0xffffffff
                        );

                        poseStack.popPose();
                    }
                });
            }
        } catch (Exception e) {
            FMAClient.LOGGER.error("rendering fail", e);
        }
    }

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
        final var res = NBTUtil.getVanityDurabilityInfo(stack);
        if (res.isEmpty() || !FMAClient.features().enableVanityDurability) {
            return;
        }

        final var data = res.get();
        final var durability = data.firstInt();
        final var maxDurability = data.secondInt();

        if (durability == maxDurability) {
            return;
        }

        RenderSystem.disableDepthTest();
        int width = Math.round((durability * 13.0f) / maxDurability);
        int color = Util.colorRange(durability, maxDurability);
        int barX = x + 2;
        int barY = y + 13;
        Graphics.fill(poseStack, barX, barY, barX + 13, barY + 2, 0xff000000);
        Graphics.fill(poseStack, barX, barY, barX + width, barY + 1, color | 0xFF000000);
        RenderSystem.enableDepthTest();
    }
}
