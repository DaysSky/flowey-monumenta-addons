package com.floweytf.fma;

import ch.njol.minecraft.config.fma.Config;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class FMAConfigMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Config.getModConfigScreenFactory("config.fma", () -> FMAClient.CONFIG, new FMAConfig());
    }
}