package com.floweytf.fma.features;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class HpIndicator {
    public static int computeEntityHealthColor(LivingEntity entity) {
        var hp = entity.getHealth();
        final var config = FMAClient.config().hpIndicator;

        if (config.countAbsorptionAsHp) {
            hp += entity.getAbsorptionAmount();
        }

        final int ratio = (int) ((hp / entity.getMaxHealth()) * 100);

        if (config.smoothColor) {
            return Util.colorRange(Mth.clamp(hp, 0, entity.getMaxHealth()), entity.getMaxHealth());
        }

        if (ratio > config.goodHpPercent) {
            return config.goodHpColor;
        }

        if (ratio > config.mediumHpPercent) {
            return config.mediumHpColor;
        }

        if (ratio > config.lowHpPercent) {
            return config.lowHpColor;
        }
        return config.criticalHpColor;
    }
}
