package com.floweytf.fma;

import com.floweytf.fma.features.Commands;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.SharedConstants;

@Config(name = "fma")
public class FMAConfig implements ConfigData {
    public static class FeatureToggles {
        public boolean enableChatChannels = true;
        public boolean enableHpIndicators = true;
        public boolean enableTimerAndStats = true;
        public boolean enableDebug = SharedConstants.IS_RUNNING_IN_IDE;

        @ConfigEntry.Gui.PrefixText
        public boolean enableIotaFix = true;
        public boolean enablePortalButtonIndicator = true;
    }

    public static class ChatAppearance {
        @ConfigEntry.ColorPicker
        public int bracketColor = 0xb7bdf8;
        @ConfigEntry.ColorPicker
        public int tagColor = 0xc6a0f6;

        public String tagText = "MAID";

        @ConfigEntry.ColorPicker
        public int textColor = 0xf4dbd6;
        @ConfigEntry.ColorPicker
        public int numericColor = 0xf38ba8;
        @ConfigEntry.ColorPicker
        public int detailColor = 0x6c6f85;
        @ConfigEntry.ColorPicker
        public int playerNameColor = 0xef9f76;
        @ConfigEntry.ColorPicker
        public int altTextColor = 0xb4befe;
    }

    public static class HpIndicator {
        public boolean enableGlowingPlayer = true;
        public boolean enableHitboxColoring = true;
        public boolean countAbsorptionAsHp = true;

        @ConfigEntry.BoundedDiscrete(max = 100)
        public int goodHpPercent = 70;
        @ConfigEntry.BoundedDiscrete(max = 100)
        public int mediumHpPercent = 50;
        @ConfigEntry.BoundedDiscrete(max = 100)
        public int lowHpPercent = 25;

        @ConfigEntry.ColorPicker
        public int goodHpColor = 0x33ef2f;
        @ConfigEntry.ColorPicker
        public int mediumHpColor = 0xd9ef2f;
        @ConfigEntry.ColorPicker
        public int lowHpColor = 0xefa22f;
        @ConfigEntry.ColorPicker
        public int criticalHpColor = 0xef472d;
    }

    @ConfigEntry.Category("features")
    @ConfigEntry.Gui.TransitiveObject
    public FeatureToggles features = new FeatureToggles();

    @ConfigEntry.Category("chatAppearance")
    @ConfigEntry.Gui.TransitiveObject
    public ChatAppearance chatAppearance = new ChatAppearance();

    @ConfigEntry.Category("hpIndicator")
    @ConfigEntry.Gui.TransitiveObject
    public HpIndicator hpIndicator = new HpIndicator();

    @Override
    public void validatePostLoad() {
        if (hpIndicator.mediumHpPercent > hpIndicator.goodHpPercent)
            hpIndicator.mediumHpPercent = hpIndicator.goodHpPercent;

        if (hpIndicator.lowHpPercent > hpIndicator.mediumHpPercent)
            hpIndicator.lowHpPercent = hpIndicator.mediumHpPercent;
    }
}
