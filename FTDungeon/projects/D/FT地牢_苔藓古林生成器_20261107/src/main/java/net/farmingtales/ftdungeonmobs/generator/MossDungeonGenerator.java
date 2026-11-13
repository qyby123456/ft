package net.farmingtales.ftdungeonmobs.generator;

import moss.generator.Engine;
import moss.generator.MossBossPortals;
import moss.generator.MossMod;
import moss.generator.Store;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MossDungeonGenerator implements DungeonGenerator {
    @Override public ResourceLocation id() { return MossBossPortals.ID; }
    @Override public Component title() { return Component.literal("苔藓古林 · 深层守望者"); }
    @Override public ItemStack icon() { return new ItemStack(Items.MOSS_BLOCK); }

    private ServerLevel dimension(MinecraftServer server) {
        ServerLevel level = server.getLevel(MossMod.DIMENSION);
        if (level == null) throw new IllegalStateException("苔藓古林维度尚未加载");
        return level;
    }

    @Override public Status status(MinecraftServer server, String instance) {
        if (instance == null || instance.isBlank()) {
            return new Status(Phase.UNPLANNED, Component.literal("尚未生成；由管理员发起古林建造"));
        }
        Engine.Job job = Store.get(dimension(server)).jobs.get(instance);
        if (job == null) return new Status(Phase.UNAVAILABLE, Component.literal("实例记录缺失，保留原门绑定等待检查"));
        return new Status(Phase.valueOf(job.stage), Component.literal("古林 " + job.stage + " · "
                + job.pieceCursor + "/" + job.plan.pieces.size() + (job.error == null ? "" : " · " + job.error)));
    }

    @Override public String reserve(Request request) {
        ServerLevel level = dimension(request.source().getServer());
        Store store = Store.get(level);
        for (Engine.Job job : store.jobs.values()) {
            if (job.plan.seed == request.seed() && "COMPLETE".equals(job.stage)) return job.plan.id;
        }
        for (Engine.Job job : store.jobs.values()) {
            if (job.plan.seed == request.seed() && !"FAILED".equals(job.stage)) return job.plan.id;
        }
        BlockPos anchor = new BlockPos(Math.multiplyExact(store.jobs.size(), 4096), 100, 0);
        return Engine.plan(level, request.seed(), anchor).plan.id;
    }

    @Override public void start(MinecraftServer server, String instance) { Engine.start(dimension(server), instance); }
    @Override public void enter(ServerPlayer player, String instance) { Engine.enter(player, instance); }
    @Override public boolean ownsDimension(ServerLevel level) { return level.dimension().equals(MossMod.DIMENSION); }
    @Override public void leave(ServerPlayer player) { MossMod.leave(player); }
}
