package com.floweytf.fma.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.floweytf.fma.features.SideBarManager;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "handleEntityEvent", at = @At(value = "HEAD", target = "Lnet/minecraft/world/entity/Entity;handleEntityEvent(B)V"))
    private void handleEntityEvent(byte status, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (status == 29) {
            SideBarManager.updateGuardTimer(player);
        }
    }
}
