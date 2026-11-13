package net.farmingtales.ftdungeonmobs.generator;

import eclipse.generator.EclipseMod;
import eclipse.generator.Engine;
import eclipse.generator.Store;
import eclipse.generator.Profiles;
import eclipse.generator.RoomSets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

final class EclipseDungeonGenerator implements DungeonGenerator {
    private final String style;

    @Override public Category category() { return Category.MEDIUM; }

    EclipseDungeonGenerator() { this(Profiles.DEFAULT); }
    EclipseDungeonGenerator(String style) { this.style = Profiles.get(style).id; }

    public ResourceLocation id() { return new ResourceLocation(style); }
    public Component title() { return Component.literal(Profiles.get(style).title); }
    public ItemStack icon() { return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(Profiles.get(style).icon))); }

    private ServerLevel dimension(MinecraftServer server) {
        ServerLevel level = server.getLevel(EclipseMod.DIMENSION);
        if (level == null) throw new IllegalStateException("月蚀维度未加载");
        return level;
    }

    public Status status(MinecraftServer server, String instance) {
        if (instance.isEmpty()) return new Status(Phase.UNPLANNED, Component.literal("尚未生成；由管理员点击生成"));
        Engine.Job job = Store.get(dimension(server)).jobs.get(instance);
        if (job == null || !style.equals(Profiles.style(job.plan))) return new Status(Phase.UNAVAILABLE, Component.literal("实例记录不存在或风格不匹配，保留门绑定供管理员检查"));
        return new Status(Phase.valueOf(job.stage), Component.literal(Profiles.get(style).title + " · " + job.stage + " · " + job.pieceCursor
                + "/" + job.plan.pieces.size() + (job.error == null ? "" : " · " + job.error)));
    }

    public String reserve(Request request) {
        ServerLevel level = dimension(request.source().getServer());
        Store store = Store.get(level);
        String roomSet = RoomSets.fingerprint(style);
        for (Engine.Job job : store.jobs.values()) if (style.equals(Profiles.style(job.plan)) && job.plan.seed == request.seed()
            && java.util.Objects.equals(roomSet, job.plan.roomSetFingerprint) && "COMPLETE".equals(job.stage)) return job.plan.id;
        for (Engine.Job job : store.jobs.values()) if (style.equals(Profiles.style(job.plan)) && job.plan.seed == request.seed()
            && java.util.Objects.equals(roomSet, job.plan.roomSetFingerprint) && !"FAILED".equals(job.stage)) return job.plan.id;
        BlockPos anchor = new BlockPos(Math.multiplyExact(store.jobs.size(), 2048), 100, 0);
        return Engine.plan(level, style, request.seed(), anchor).plan.id;
    }

    private void checkInstance(MinecraftServer server, String instance) {
        Engine.Job job = Store.get(dimension(server)).jobs.get(instance);
        if (job == null || !style.equals(Profiles.style(job.plan))) throw new IllegalStateException("实例不属于此中等地牢风格");
    }

    public void start(MinecraftServer server, String instance) {
        checkInstance(server, instance);
        Engine.start(dimension(server), instance);
    }
    public void enter(ServerPlayer player, String instance) {
        checkInstance(player.server, instance);
        Engine.enter(player, instance);
    }
    public boolean ownsDimension(ServerLevel level) { return level.dimension().equals(EclipseMod.DIMENSION); }
    public void leave(ServerPlayer player) { EclipseMod.leave(player); }
}
