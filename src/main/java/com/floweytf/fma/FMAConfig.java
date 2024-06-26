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

    @Category("hpIndicator")
    public boolean enableGlowingPlayer = true;

    @Category("hpIndicator")
    public boolean enableHitboxColoring = true;

    @Category("hpIndicator")
    public boolean countAbsorptionAsHp = true;

    @Category("hpIndicator")
    @Color
    public int goodHpColor = 0x33ef2f;
    @Category("hpIndicator")
    @FloatSlider(min = 0, max = 100, step = 5)
    public float goodHpPercent = 60;

    @Category("hpIndicator")
    @Color
    public int mediumHpColor = 0xd9ef2f;
    @Category("hpIndicator")
    @FloatSlider(min = 0, max = 100, step = 5)
    public float mediumHpPercent = 30;

    @Category("hpIndicator")
    @Color
    public int lowHpColor = 0xefa22f;
    @Category("hpIndicator")
    @FloatSlider(min = 0, max = 100, step = 5)
    public float lowHpPercent = 10;

    @Category("hpIndicator")
    @Color
    public int criticalHpColor = 0xef472d;

    @Category("chatAppearance")
    @Color
    public int bracketColor = 0xb7bdf8;

    @Category("chatAppearance")
    @Color
    public int tagColor = 0xc6a0f6;

    @Category("chatAppearance")
    @Color
    public String tagText = "MAID";

    @Category("chatAppearance")
    @Color
    public int textColor = 0xf4dbd6;

    @Category("chatAppearance")
    @Color
    public int numericColor = 0xf38ba8;
}
