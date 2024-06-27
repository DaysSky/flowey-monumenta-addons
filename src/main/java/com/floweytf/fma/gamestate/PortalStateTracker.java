package com.floweytf.fma.gamestate;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.ChatUtil;
import com.floweytf.fma.util.StatsUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;

import java.util.Objects;

import static com.floweytf.fma.util.Util.now;

public class PortalStateTracker implements StateTracker {
    public static class Data {
        @StatsUtil.Detail
        public int soulCount = -1;
        public int chestCount = -1;

        @StatsUtil.Time
        public int totalTime = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int soulTime = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int nodesSplit = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int startBossSplit = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int bossSplit = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int phase1Split = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int phase2Split = -1;

        @StatsUtil.Detail
        @StatsUtil.Time
        public int phase3Split = -1;

        public void send() {
            StatsUtil.dumpStats("stat.fma.portal", this);
        }
    }

    private final long startTime;

    private long nodesSplit;
    private long startBossSplit;
    private long phase1Split;
    private long phase2Split;

    private final Data data;

    private boolean enteredBoss = false;
    private boolean isCubeAlive = false;
    private boolean hasWon = false;

    public PortalStateTracker() {
        this.data = new Data();
        startTime = now();
    }

    @Override
    public void onChatMessage(Component message) {
        if (!FMAClient.CONFIG.get().features.enableTimerAndStats) {
            return;
        }

        final var raw = message.getString();

        if (raw.length() < 12)
            return;

        switch (raw.substring(0, 12)) {
        case "Doorway prot":
            nodesSplit = now();
            data.nodesSplit = StatsUtil.logTime("timer.fma.portal.nodes", startTime, startTime, nodesSplit);
            break;
        case "[Iota] INTRU":
            enteredBoss = true;
            startBossSplit = now();
            data.startBossSplit = StatsUtil.logTime("timer.fma.portal.startBoss", startTime, nodesSplit,
                startBossSplit);
            break;
        case "[Iota] DAMAG":
            phase1Split = now();
            data.phase1Split = (int) (phase1Split - startBossSplit);
            break;
        case "[Iota] DAMA9":
            phase2Split = now();
            data.phase2Split = (int) (phase2Split - phase1Split);
            break;
        case "[Iota] DESTR":
            long phase3Split = now();
            data.phase3Split = (int) (phase3Split - phase2Split);
            data.totalTime = (int) (phase3Split - startTime);
            data.bossSplit = StatsUtil.logTime(
                "timer.fma.portal.boss",
                startTime, nodesSplit,
                phase1Split,
                phase2Split,
                phase3Split,
                phase3Split
            );
            hasWon = true;
            data.send();
            break;
        }
    }

    @Override
    public void onLeave() {
        if (!FMAClient.CONFIG.get().features.enableTimerAndStats) {
            return;
        }

        if (!hasWon) {
            data.send();
            StatsUtil.dumpStats("stat.fma.portal", data);
        }
    }

    @Override
    public void onActionBar(Component message) {
        if (!FMAClient.CONFIG.get().features.enableTimerAndStats) {
            return;
        }

        final var raw = message.getString();
        if (raw.contains("S.O.U.L. Collected")) {
            final var parts = raw.split(" : ");
            if (parts.length != 2) {
                ChatUtil.sendDebug("soul count parsing fail");
                return;
            }

            final var counterText = parts[1];

            if (!counterText.contains("/")) {
                ChatUtil.sendDebug("soul count parsing fail");
                return;
            }

            data.soulCount = Integer.parseInt(counterText.substring(0, counterText.indexOf("/")));

            if (data.soulCount >= 350 && data.soulTime == -1) {
                data.soulTime = StatsUtil.logTime(
                    "timer.fma.portal.souls",
                    startTime,
                    startTime,
                    now()
                );
            }
        } else if (raw.contains("total chests")) {
            data.chestCount = Integer.parseInt(raw.split(" ")[0]);
        }
    }

    @Override
    public void exportDebugInfo() {
        ChatUtil.send(Component.literal("PortalStateTracker").withStyle(ChatFormatting.UNDERLINE));
    }

    @Override
    public void onTick() {
        if (!enteredBoss) {
            return;
        }

        isCubeAlive = false;

        // Stupid IOTA bug!
        // I'm sure this code will work perfectly fine, right?!
        FMAClient.level().entitiesForRendering().forEach(entity -> {
            if (FMAClient.CONFIG.get().features.enableIotaFix &&
                entity.getName().getString().contains("Iota") &&
                !entity.isInvisible() &&
                entity.getPosition(0).y < 88
            ) {
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
        if (!FMAClient.CONFIG.get().features.enablePortalButtonIndicator) {
            return;
        }

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
