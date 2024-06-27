package com.floweytf.fma.features;

import com.floweytf.fma.events.ClientSetTitleEvent;
import com.floweytf.fma.events.EventResult;

public class DeathTracker {
    private int deathCount = 0;

    public DeathTracker() {
        ClientSetTitleEvent.TITLE.register(text -> {
            if (text.getString().equals("You Died"))
                deathCount++;

            return EventResult.CONTINUE;
        });
    }
}
