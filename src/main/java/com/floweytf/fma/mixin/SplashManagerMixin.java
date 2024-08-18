package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import java.util.Random;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashManager.class)
public class SplashManagerMixin {
    @Inject(
        method = "getSplash",
        at = @At("HEAD"),
        cancellable = true
    )
    private void modifySplash(CallbackInfoReturnable<SplashRenderer> cir) {
        if (FMAClient.features().enableCustomSplash) {
            cir.setReturnValue(new SplashRenderer(FMAClient.SPLASH.get(new Random().nextInt(FMAClient.SPLASH.size()))));
        }
    }
}
