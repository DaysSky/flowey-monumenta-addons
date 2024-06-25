package com.floweytf.fma.gamestate;

import com.floweytf.fma.util.SplitTiming;
import com.floweytf.fma.util.Stopwatch;
import com.floweytf.fma.util.Timestamp;
import com.floweytf.fma.util.Utils;
import net.minecraft.network.chat.Component;

public class RuinStateTracker implements StateTracker {
    public record Data(
        int chestCount,
        @Timestamp int totalTime,
        @Timestamp int mobsTime,
        @Timestamp int bossSplitTime,
        @Timestamp int daggersSplitTime,
        @Timestamp int dps1SplitTime
    ) {
        public void send() {
            Utils.dumpStats("stats.fma.ruin", this);
        }
    }

    private final Stopwatch totalTime = new Stopwatch();
    private final Stopwatch splitStopwatch = new Stopwatch();

    private final SplitTiming mobs = new SplitTiming(splitStopwatch, totalTime, "timer.fma.ruin.mobs");
    private final SplitTiming boss = new SplitTiming(splitStopwatch, totalTime, "timer.fma.ruin.boss");
    private final SplitTiming daggers = new SplitTiming(splitStopwatch, totalTime, "timer.fma.ruin.daggers");
    private final SplitTiming dps = new SplitTiming(splitStopwatch, totalTime, "timer.fma.ruin.dps");

    private int chestCount = 0;

    public RuinStateTracker() {
    }

    @Override
    public void onChatMessage(Component message) {
        final var raw = message.getString();
        if (raw.length() < 12)
            return;

        switch (raw.substring(0, 12)) {
        case "Something in" -> mobs.done();
        case "[Samwell] We" -> boss.done();
        case "[Samwell] I'" -> daggers.done();
        case "[Samwell] I." -> {
            dps.done();
            final var data = new Data(
                chestCount,
                totalTime.time(),
                mobs.value(),
                boss.value(),
                daggers.value(),
                dps.value()
            );

            data.send();
        }
        }
    }

    @Override
    public void onActionBar(Component message) {
        final var raw = message.getString();
        if (raw.contains("total chests")) {
            chestCount = Integer.parseInt(raw.split(" ")[0]);
        }
    }

    @Override
    public void exportDebugInfo() {
    }
}
