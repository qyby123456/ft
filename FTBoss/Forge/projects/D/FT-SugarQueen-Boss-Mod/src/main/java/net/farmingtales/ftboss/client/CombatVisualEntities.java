package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.util.LevelTickCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CombatVisualEntities {
    public record Snapshot(List<EasterBossEntity> hiddenBosses, List<Entity> orbLinks, List<Entity> flywheels) {}

    private static final Snapshot EMPTY = new Snapshot(List.of(), List.of(), List.of());
    private static final LevelTickCache<Snapshot> CACHE = new LevelTickCache<>();

    private CombatVisualEntities() {}

    public static Snapshot get(ClientLevel level) {
        if (level == null) {
            CACHE.clear();
            return EMPTY;
        }
        return CACHE.get(level, level.getGameTime(), () -> collect(level));
    }

    private static Snapshot collect(ClientLevel level) {
        List<EasterBossEntity> hiddenBosses = new ArrayList<>();
        List<Entity> orbLinks = new ArrayList<>();
        List<Entity> flywheels = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity.isRemoved()) continue;
            if (entity instanceof EasterBossEntity boss) hiddenBosses.add(boss);
            else if (entity instanceof DefectCoreEntity || entity instanceof ChargedOrbEntity) orbLinks.add(entity);
            else if (entity instanceof FlywheelProjectileEntity || entity instanceof PlayerMoonChakramEntity) flywheels.add(entity);
        }
        if (hiddenBosses.isEmpty() && orbLinks.isEmpty() && flywheels.isEmpty()) return EMPTY;
        return new Snapshot(List.copyOf(hiddenBosses), List.copyOf(orbLinks), List.copyOf(flywheels));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level == null) CACHE.clear();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        CACHE.clear(event.getLevel());
    }
}
