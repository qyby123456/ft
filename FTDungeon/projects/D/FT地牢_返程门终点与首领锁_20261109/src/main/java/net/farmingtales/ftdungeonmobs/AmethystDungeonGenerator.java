package net.farmingtales.ftdungeonmobs;

import cn.ft.biomedungeon.runtime.BiomeDungeonBackend;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class AmethystDungeonGenerator implements DungeonGenerators.Provider {
    public static final BlockPos ANCHOR = new BlockPos(1000, 200, 1000);

    public AmethystDungeonGenerator() { MinecraftForge.EVENT_BUS.register(this); }
    public ResourceLocation id() { return DungeonGenerators.AMETHYST_BIOME; }
    public Component name() { return Component.translatable("generator.ftdungeonmobs.amethyst_biome"); }
    public Component description() { return Component.translatable("generator.ftdungeonmobs.amethyst_biome.description"); }

    public DungeonGenerators.Preparation prepare(ServerLevel destination) {
        var status = BiomeDungeonBackend.prepare(destination, ANCHOR, 0);
        if (status.state() == BiomeDungeonBackend.State.READY && returnGate(destination, status)) {
            return new DungeonGenerators.Preparation(DungeonGenerators.State.READY, status.landing(),
                    Component.translatable("message.ftdungeonmobs.gate.generator_ready"));
        }
        boolean building = status.state() == BiomeDungeonBackend.State.BUILDING;
        return new DungeonGenerators.Preparation(building ? DungeonGenerators.State.BUILDING : DungeonGenerators.State.FAILED,
                null, Component.translatable("message.ftdungeonmobs.gate." + (building ? "generator_building" : "generator_failed")));
    }

    private static boolean returnGate(ServerLevel level, BiomeDungeonBackend.Status status) {
        try {
            var encounter = BiomeDungeonBackend.prepareExit(level);
            return level.getBlockEntity(BlockPos.of(encounter.gate)) instanceof DungeonGateBlockEntity;
        } catch (RuntimeException failure) {
            com.mojang.logging.LogUtils.getLogger().error("Cannot prepare sealed dungeon exit", failure);
            return false;
        }
    }

    public void tick(MinecraftServer server) { BiomeDungeonBackend.tick(server); }

    @SubscribeEvent
    public void stop(ServerStoppingEvent event) { BiomeDungeonBackend.close(event.getServer()); }

    @SubscribeEvent
    public void breaking(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && BiomeDungeonBackend.protects(level, event.getPos())) {
            event.setCanceled(true);
            BiomeDungeonBackend.externalChange(level, event.getPos(), "block break");
        }
    }

    @SubscribeEvent
    public void placing(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (event instanceof BlockEvent.EntityMultiPlaceEvent multiple) {
                for (var snapshot : multiple.getReplacedBlockSnapshots()) {
                    if (BiomeDungeonBackend.protects(level, snapshot.getPos())) {
                        event.setCanceled(true);
                        BiomeDungeonBackend.externalChange(level, snapshot.getPos(), "multi place");
                        return;
                    }
                }
            } else if (BiomeDungeonBackend.protects(level, event.getPos())) {
                event.setCanceled(true);
                BiomeDungeonBackend.externalChange(level, event.getPos(), "block place");
            }
        }
    }

    @SubscribeEvent
    public void bucket(FillBucketEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getTarget() instanceof net.minecraft.world.phys.BlockHitResult hit) {
            for (BlockPos position : new BlockPos[]{hit.getBlockPos(), hit.getBlockPos().relative(hit.getDirection())}) {
                if (BiomeDungeonBackend.protects(level, position)) {
                    event.setCanceled(true);
                    BiomeDungeonBackend.externalChange(level, position, "bucket");
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void explosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            var affected = event.getAffectedBlocks().stream().filter(position -> BiomeDungeonBackend.protects(level, position)).toList();
            if (!affected.isEmpty()) {
                event.getAffectedBlocks().removeAll(affected);
                BiomeDungeonBackend.externalChange(level, affected.get(0), "explosion");
            }
        }
    }

    @SubscribeEvent
    public void piston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) { return; }
        var resolver = event.getStructureHelper();
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();
        positions.add(event.getPos());
        positions.add(event.getPos().relative(event.getDirection()));
        if (resolver != null && resolver.resolve()) {
            positions.addAll(resolver.getToPush());
            positions.addAll(resolver.getToDestroy());
            for (BlockPos position : resolver.getToPush()) { positions.add(position.relative(event.getDirection())); }
        }
        for (BlockPos position : positions) {
            if (BiomeDungeonBackend.protects(level, position)) {
                event.setCanceled(true);
                BiomeDungeonBackend.externalChange(level, position, "piston");
                return;
            }
        }
    }
}
