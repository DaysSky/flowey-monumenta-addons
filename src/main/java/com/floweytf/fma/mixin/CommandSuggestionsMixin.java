package com.floweytf.fma.mixin;

import com.floweytf.fma.features.Commands;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
    @Shadow
    @Final
    EditBox input;

    @SuppressWarnings("unchecked")
    @ModifyExpressionValue(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;getCommands()" +
                "Lcom/mojang/brigadier/CommandDispatcher;"
        )
    )
    private CommandDispatcher<SharedSuggestionProvider> fma$suggest(CommandDispatcher<SharedSuggestionProvider> val) {
        if (input.getValue().isEmpty()) {
            return val;
        }

        if (Commands.suggestAccepts(input.getValue().substring(1))) {
            return (CommandDispatcher<SharedSuggestionProvider>) (CommandDispatcher<?>) Commands.getDispatcher();
        }

        return val;
    }
}
