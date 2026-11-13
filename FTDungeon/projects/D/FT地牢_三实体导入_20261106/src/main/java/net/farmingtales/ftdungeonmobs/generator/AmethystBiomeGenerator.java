package net.farmingtales.ftdungeonmobs.generator;

import cn.ft.biomedungeon.runtime.BiomeDungeonBackend;
import net.farmingtales.ftdungeonmobs.DungeonGateTravel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class AmethystBiomeGenerator implements DungeonGenerator {
    private static final String INSTANCE = "amethyst-biome-side-porch2";

    public ResourceLocation id() { return net.farmingtales.ftdungeonmobs.DungeonGenerators.AMETHYST_BIOME; }
    public Component title() { return Component.literal("紫晶群系 · 九房连接地牢"); }
    public ItemStack icon() { return new ItemStack(Items.AMETHYST_SHARD); }

    private ServerLevel dimension(MinecraftServer server) {
        ServerLevel level = server.getLevel(DungeonGateTravel.DUNGEON);
        if (level == null) { throw new IllegalStateException("FT 地牢维度未加载"); }
        return level;
    }

    public Status status(MinecraftServer server, String instance) {
        if (instance.isEmpty()) { return new Status(Phase.UNPLANNED, Component.literal("由管理员点击生成；完成施工与原生观察后开放入口")); }
        if (!INSTANCE.equals(instance)) { return new Status(Phase.UNAVAILABLE, Component.literal("紫晶实例绑定不匹配，保留现场")); }
        var status = BiomeDungeonBackend.current(dimension(server));
        Phase phase = switch (status.state()) {
            case IDLE -> Phase.PLANNED;
            case BUILDING -> Phase.BUILDING;
            case READY -> Phase.COMPLETE;
            case FAILED -> Phase.FAILED;
        };
        return new Status(phase, Component.literal(status.stage() + " · " + status.completed() + "/" + status.total()
                + " · " + status.observedTicks() + "/2400 ticks " + status.error()));
    }

    public String reserve(Request request) { dimension(request.source().getServer()); return INSTANCE; }

    public void start(MinecraftServer server, String instance) {
        if (!INSTANCE.equals(instance)) { throw new IllegalStateException("紫晶实例绑定不匹配"); }
        var preparation = net.farmingtales.ftdungeonmobs.DungeonGenerators.get(id()).prepare(dimension(server));
        if (preparation.state() == net.farmingtales.ftdungeonmobs.DungeonGenerators.State.FAILED) {
            throw new IllegalStateException(preparation.message().getString());
        }
    }

    public void enter(ServerPlayer player, String instance) {
        throw new IllegalStateException("紫晶群系必须通过完整 FT 入口门入场");
    }
}
