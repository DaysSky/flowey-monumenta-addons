package com.floweytf.fma;

import ch.njol.minecraft.config.fma.Options;
import ch.njol.minecraft.config.fma.annotations.Category;
import ch.njol.minecraft.config.fma.annotations.Color;
import ch.njol.minecraft.config.fma.annotations.FloatSlider;
import com.floweytf.fma.registry.Commands;
import net.minecraft.SharedConstants;

public class FMAConfig implements Options {
    @Override
    public void onUpdate() {
        FMAClient.saveConfig();
        Commands.load();
    }

    @Category("chat")
    public boolean enableChatChannels = true;

    @Category("timers")
    public boolean enableTimers = true;

    @Category("misc")
    public boolean enableDebug = SharedConstants.IS_RUNNING_IN_IDE;

    @Category("misc")
    public boolean enableIotaFix = true;

    @Category("hp_indicator")
    public boolean enableGlowingPlayer = true;

    @Category("hp_indicator")
    public boolean enableHitboxColoring = true;

    @Category("hp_indicator")
    public boolean countAbsorptionAsHp = true;

    @Category("hp_indicator")
    @Color
    public int goodHpColor = 0x33ef2f;
    @Category("hp_indicator")
    @FloatSlider(min = 0, max = 100, step = 5)
    public float goodHpPercent = 60;

    @Category("hp_indicator")
    @Color
    public int mediumHpColor = 0xd9ef2f;
    @Category("hp_indicator")
    @FloatSlider(min = 0, max = 100, step = 5)
    public float mediumHpPercent = 30;

    @Category("hp_indicator")
    @Color
    public int lowHpColor = 0xefa22f;
    @Category("hp_indicator")
    @FloatSlider(min = 0, max = 100, step = 5)
    public float lowHpPercent = 10;

    @Category("hp_indicator")
    @Color
    public int criticalHpColor = 0xef472d;
}
