package com.floweytf.fma.gamestate;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.SplitTiming;
import com.floweytf.fma.util.Stopwatch;
import com.floweytf.fma.util.Timestamp;
import com.floweytf.fma.util.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;

import java.util.Objects;

public class PortalStateTracker implements StateTracker {
    public record Data(
        int soulCount,
        int chestCount,
        @Timestamp int totalTime,
        @Timestamp int soulTime,
        @Timestamp int nodesSplitTime,
        @Timestamp int bossSplitTime,
        @Timestamp int phase1SplitTime,
        @Timestamp int phase2SplitTime,
        @Timestamp int phase3SplitTime
    ) {
        public void send() {
            Utils.dumpStats("stats.fma.portal", this);
        }
    }

    private final Stopwatch totalTime = new Stopwatch();
    private final Stopwatch splitStopwatch = new Stopwatch();

    private final SplitTiming soulsSplit = new SplitTiming(new Stopwatch(), totalTime, "timer.fma.portal.souls");
    private final SplitTiming nodeSplit = new SplitTiming(splitStopwatch, totalTime, "timer.fma.portal.nodes");
    private final SplitTiming bossSplit = new SplitTiming(splitStopwatch, totalTime, "timer.fma.portal.boss_start");
    private final SplitTiming phase1Split = new SplitTiming(splitStopwatch, totalTime, "timer.fma.portal.phase1");
    private final SplitTiming phase2Split = new SplitTiming(splitStopwatch, totalTime, "timer.fma.portal.phase2");
    private final SplitTiming phase3Split = new SplitTiming(splitStopwatch, totalTime, "timer.fma.portal.phase3");

    private int soulCount = 0;
    private int chestCount = 0;
    private boolean enteredBoss = false;
    private boolean isCubeAlive = false;

    public PortalStateTracker() {
    }

    @Override
    public void onChatMessage(Component message) {
        final var raw = message.getString();

        if (raw.length() < 12)
            return;

        switch (raw.substring(0, 12)) {
        case "Doorway prot" -> nodeSplit.done();
        case "[Iota] INTRU" -> {
            enteredBoss = true;
            bossSplit.done();
        }
        case "[Iota] DAMAG" -> phase1Split.done();
        case "[Iota] DAMA9" -> phase2Split.done();
        case "[Iota] DESTR" -> {
            phase3Split.done();
            // save the report
            final var data = new Data(
                soulCount,
                chestCount,
                totalTime.time(),
                soulsSplit.value(),
                nodeSplit.value(),
                bossSplit.value(),
                phase1Split.value(),
                phase2Split.value(),
                phase3Split.value()
            );
            data.send();
        }
        }
    }

    @Override
    public void onActionBar(Component message) {
        final var raw = message.getString();
        if (raw.contains("S.O.U.L. Collected")) {
            final var parts = raw.split(" : ");
            if (parts.length != 2) {
                Utils.sendDebug("soul count parsing fail");
                return;
            }

            final var counterText = parts[1];

            if (!counterText.contains("/")) {
                Utils.sendDebug("soul count parsing fail");
                return;
            }

            soulCount = Integer.parseInt(counterText.substring(0, counterText.indexOf("/")));

            if (soulCount > 350) {
                soulsSplit.done();
            }
        } else if (raw.contains("total chests")) {
            chestCount = Integer.parseInt(raw.split(" ")[0]);
        }
    }

    @Override
    public void exportDebugInfo() {
        Utils.send(Component.literal("PortalStateTracker").withStyle(ChatFormatting.UNDERLINE));
        Utils.send("totalTime = " + totalTime);
        Utils.send("splitStopwatch = " + splitStopwatch);
        Utils.send("souls = " + soulsSplit);
        Utils.send("nodes = " + nodeSplit);
        Utils.send("boss = " + bossSplit);
        Utils.send("phase1 = " + phase1Split);
        Utils.send("phase2 = " + phase2Split);
        Utils.send("phase3 = " + phase3Split);
        Utils.send("soulCount = " + soulCount);
        Utils.send("chestCount = " + chestCount);
    }

    @Override
    public void onTick() {
        if (!enteredBoss) {
            return;
        }

        isCubeAlive = false;

        // Stupid IOTA bug!
        // I'm sure this code will work perfectly fine, right?!
        ((ClientLevel) Utils.player().level).entitiesForRendering().forEach(entity -> {
            if (entity.getName().getString().contains("Iota") &&
                !entity.isInvisible() &&
                entity.getPosition(0).y < 88 &&
                FMAClient.CONFIG.enableIotaFix) {
                entity.moveTo(entity.getX(), 89, entity.getZ());
            }

            if (entity instanceof ArmorStand armorStand) {
                final var slot = armorStand.getItemBySlot(EquipmentSlot.HEAD);
                if (slot.isEmpty()) {
                    return;
                }

                if (slot.getItem() != Items.PLAYER_HEAD) {
                    return;
                }

                if (slot.getOrCreateTag().toString().contains("eyJ0ZXh0dXJlcyI")) {
                    isCubeAlive = true;
                }
            }
        });
    }

    // Render a box around buttons
    @Override
    public void onRender(WorldRenderContext context) {
        int r = isCubeAlive ? 1 : 0;
        int g = isCubeAlive ? 0 : 1;
        final var buffer = Objects.requireNonNull(context.consumers()).getBuffer(RenderType.lines());
        final var pose = context.matrixStack();

        if (enteredBoss) {
            LevelRenderer.renderLineBox(pose, buffer,
                1057, 87, 1388,  // button pos
                1057 + 1, 87 + 1, 1388 + 1,
                r, g, 0, 1
            );

            LevelRenderer.renderLineBox(pose, buffer,
                1005, 87, 1388,  // button pos
                1005 + 1, 87 + 1, 1388 + 1,
                r, g, 0, 1
            );
        }
    }
}
