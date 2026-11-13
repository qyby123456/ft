package net.farmingtales.ftdungeonmobs.generator;

import moss.generator.Engine;
import moss.generator.MossBossPortals;
import moss.generator.MossMod;
import moss.generator.Store;
import moss.generator.Profiles;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MossDungeonGenerator implements DungeonGenerator {
    private final String style;

    public MossDungeonGenerator() { this("ftmossgen:forest"); }

    public MossDungeonGenerator(String style) {
        this.style = Profiles.get(style).id;
    }

    @Override public Category category() { return Category.LEGACY_STYLE; }
    @Override public ResourceLocation id() { return new ResourceLocation(Profiles.get(style).id); }
    @Override public Component title() { return Component.literal(Profiles.get(style).title); }
    @Override public ItemStack icon() {
        ResourceLocation icon = new ResourceLocation(Profiles.get(style).icon);
        if (!ForgeRegistries.ITEMS.containsKey(icon)) throw new IllegalStateException("Unknown style icon: " + icon);
        ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(icon));
        if (stack.isEmpty()) throw new IllegalStateException("Style icon cannot be AIR: " + icon);
        return stack;
    }

    private ServerLevel dimension(MinecraftServer server) {
        ServerLevel level = server.getLevel(MossMod.DIMENSION);
        if (level == null) throw new IllegalStateException("苔藓古林维度尚未加载");
        return level;
    }

    @Override public Status status(MinecraftServer server, String instance) {
        if (instance == null || instance.isBlank()) {
            return new Status(Phase.UNPLANNED, Component.literal("尚未生成；由管理员发起此风格建造"));
        }
        Engine.Job job = Store.get(dimension(server)).jobs.get(instance);
        if (job == null || !style.equals(job.plan.style)) return new Status(Phase.UNAVAILABLE, Component.literal("实例记录缺失，保留原门绑定等待检查"));
        return new Status(Phase.valueOf(job.stage), Component.literal(Profiles.get(style).title + " " + job.stage + " · "
                + job.pieceCursor + "/" + job.plan.pieces.size() + (job.error == null ? "" : " · " + job.error)));
    }

    @Override public String reserve(Request request) {
        ServerLevel level = dimension(request.source().getServer());
        Store store = Store.get(level);
        for (Engine.Job job : store.jobs.values()) {
            if (style.equals(job.plan.style) && job.plan.seed == request.seed() && "COMPLETE".equals(job.stage)) return job.plan.id;
        }
        for (Engine.Job job : store.jobs.values()) {
            if (style.equals(job.plan.style) && job.plan.seed == request.seed() && !"FAILED".equals(job.stage)) return job.plan.id;
        }
        BlockPos anchor = new BlockPos(Math.multiplyExact(store.jobs.size(), 4096), 100, 0);
        return Engine.plan(level, style, request.seed(), anchor).plan.id;
    }

    private void checkInstance(MinecraftServer server, String instance) {
        Engine.Job job = Store.get(dimension(server)).jobs.get(instance);
        if (job == null || !style.equals(job.plan.style)) throw new IllegalStateException("实例不属于此风格");
    }

    @Override public void start(MinecraftServer server, String instance) {
        checkInstance(server, instance);
        Engine.start(dimension(server), instance);
    }
    @Override public void enter(ServerPlayer player, String instance) {
        checkInstance(player.server, instance);
        Engine.enter(player, instance);
    }
    @Override public boolean ownsDimension(ServerLevel level) { return level.dimension().equals(MossMod.DIMENSION); }
    @Override public void leave(ServerPlayer player) { MossMod.leave(player); }
}
