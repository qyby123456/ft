package net.farmingtales.ftdungeonmobs.generator;

import cn.ft.biomedungeon.runtime.SmallCatalog;
import cn.ft.biomedungeon.runtime.SmallDungeons;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SmallDungeonGenerator implements DungeonGenerator {
    private final String style;
    public SmallDungeonGenerator(String style) { this.style = SmallCatalog.style(style).id(); }
    @Override public ResourceLocation id() { return new ResourceLocation("ftsmall", style); }
    @Override public Category category() { return Category.SMALL; }
    @Override public Component title() { return Component.literal(SmallCatalog.style(style).title() + " · 九房群系地牢"); }
    @Override public ItemStack icon() { return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(SmallCatalog.style(style).icon()))); }
    private ServerLevel dimension(MinecraftServer server) {
        ServerLevel level = server.getLevel(SmallDungeons.DIMENSION);
        if (level == null) throw new IllegalStateException("小型地牢维度未加载，请完整更新模组后重新打开世界");
        return level;
    }
    @Override public Status status(MinecraftServer server, String instance) {
        if (instance.isEmpty()) return new Status(Phase.UNPLANNED, Component.literal("独立九房池 · 每间仅一次 · 随机连接 · 外围与房间隔离"));
        try {
            var job = SmallDungeons.require(dimension(server), style, instance);
            return new Status(Phase.valueOf(job.phase), Component.literal(title().getString() + " · " + job.phase + " · 九间独立房间 · " + job.cursor + " 方块" + (job.error.isEmpty() ? "" : " · " + job.error)));
        } catch (RuntimeException failure) { return new Status(Phase.UNAVAILABLE, Component.literal(failure.getMessage())); }
    }
    @Override public String reserve(Request request) { return SmallDungeons.reserve(dimension(request.source().getServer()), style, request.seed()).plan.instanceId(); }
    @Override public void start(MinecraftServer server, String instance) {
        ServerLevel level = dimension(server);
        SmallDungeons.start(level, SmallDungeons.require(level, style, instance));
    }
    @Override public void enter(ServerPlayer player, String instance) {
        ServerLevel level = dimension(player.server);
        SmallDungeons.enter(player, level, SmallDungeons.require(level, style, instance));
    }
    @Override public boolean ownsDimension(ServerLevel level) { return level.dimension().equals(SmallDungeons.DIMENSION); }
    @Override public void leave(ServerPlayer player) { SmallDungeons.leave(player); }
}
