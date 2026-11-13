package net.farmingtales.teamlag;

import java.util.ArrayDeque;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineTiming {
    private static final ArrayDeque<Frame> FRAMES = new ArrayDeque<>();
    private static final ArrayDeque<Frame> FREE = new ArrayDeque<>();
    private static final class Frame {
        Object owner;
        ServerLevel level;
        net.minecraft.core.BlockPos pos;
        long start;
        boolean counted;
        long children;
    }
    private MachineTiming() {}
    private static void push(Object owner, ServerLevel level, net.minecraft.core.BlockPos pos) {
        Frame frame = FREE.pollFirst();
        if (frame == null) frame = new Frame();
        frame.owner = owner; frame.level = level; frame.pos = pos; frame.children = 0; frame.start = System.nanoTime();
        frame.counted = !(owner instanceof Player);
        FRAMES.push(frame);
    }
    public static void begin(BlockEntity entity) {
        if (entity != null && entity.getLevel() instanceof ServerLevel level && level.getServer() == LagQuery.server() && level.getServer().isSameThread())
            push(entity, level, entity.getBlockPos());
    }
    public static void begin(Entity entity) {
        if (entity != null && entity.level() instanceof ServerLevel level && level.getServer() == LagQuery.server() && level.getServer().isSameThread())
            push(entity, level, entity.blockPosition());
    }
    public static void end(Object owner) {
        if (FRAMES.isEmpty() || FRAMES.peek().owner != owner) return;
        Frame frame = FRAMES.pop();
        long elapsed = Math.max(0, System.nanoTime() - frame.start);
        if (!FRAMES.isEmpty()) FRAMES.peek().children += elapsed;
        if (frame.counted) LagQuery.record(frame.level, frame.pos, Math.max(0, elapsed - frame.children));
        frame.owner = null; frame.level = null; frame.pos = null; FREE.addFirst(frame);
    }
    static void clear() { FRAMES.clear(); }
}
