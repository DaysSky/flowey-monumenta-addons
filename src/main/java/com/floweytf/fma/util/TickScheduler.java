package com.floweytf.fma.util;

import com.floweytf.fma.debug.DebugInfoExporter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;
import java.util.function.Consumer;

public class TickScheduler implements DebugInfoExporter {
    public interface TaskControl {
        void cancel();
    }

    private record Task(long targetTick, Consumer<Minecraft> handler) implements Comparable<Task> {
        @Override
        public int compareTo(@NotNull TickScheduler.Task task) {
            return (int) (targetTick - task.targetTick);
        }
    }

    private long tick;
    private final PriorityQueue<Task> taskQueue = new PriorityQueue<>();

    public TickScheduler() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            tick++;
            while (!taskQueue.isEmpty() && taskQueue.peek().targetTick < tick) {
                taskQueue.poll().handler.accept(client);
            }
        });
    }

    private void doCancel(Task task) {
        taskQueue.remove(task);
    }

    public TaskControl schedule(int delay, Consumer<Minecraft> handler) {
        final var task = new Task(tick + delay, handler);
        taskQueue.add(task);
        return () -> doCancel(task);
    }

    @Override
    public void exportDebugInfo() {
        ChatUtil.send(Component.literal("TickScheduler").withStyle(ChatFormatting.UNDERLINE));
        ChatUtil.send("queueSize = " + taskQueue.size());
        ChatUtil.send("tick = " + tick);
    }
}
