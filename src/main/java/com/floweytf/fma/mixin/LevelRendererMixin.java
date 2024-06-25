package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.Utils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @ModifyExpressionValue(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private boolean modifyPlayerGlowingStatus(boolean original, @Local Entity entity) {
        if (!FMAClient.CONFIG.enableGlowingPlayer) {
            return original;
        }

        if (entity instanceof Player) {
            return true;
        }

        return original;
    }

    @ModifyExpressionValue(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"
        )
    )
    private int modifyPlayerGlowingColor(int original, @Local Entity entity) {
        if (!FMAClient.CONFIG.enableGlowingPlayer) {
            return original;
        }

        if (entity instanceof Player player) {
            return Utils.computeEntityHealthColor(player);
        }

        return original;
    }
}
