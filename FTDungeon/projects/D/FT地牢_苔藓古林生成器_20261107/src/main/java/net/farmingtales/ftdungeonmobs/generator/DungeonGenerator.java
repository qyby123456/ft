package net.farmingtales.ftdungeonmobs.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface DungeonGenerator {
    enum Phase { UNPLANNED, PLANNED, CHECKING, BUILDING, INITIALIZING, PAUSED, COMPLETE, FAILED, UNAVAILABLE }

    record Status(Phase phase, Component detail) {
        public boolean ready() { return phase == Phase.COMPLETE; }
    }

    record Request(ServerLevel source, BlockPos gate, long seed, int tier) {}

    ResourceLocation id();
    Component title();
    ItemStack icon();
    Status status(MinecraftServer server, String instance);
    String reserve(Request request);
    void start(MinecraftServer server, String instance);
    void enter(ServerPlayer player, String instance);
    default boolean ownsDimension(ServerLevel level) { return false; }
    default void leave(ServerPlayer player) { throw new IllegalStateException("生成器没有提供返回功能"); }
}
