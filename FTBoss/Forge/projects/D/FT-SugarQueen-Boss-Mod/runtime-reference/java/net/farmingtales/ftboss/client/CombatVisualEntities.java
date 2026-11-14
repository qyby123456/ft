/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class CombatVisualEntities {
    private static final Snapshot EMPTY = new Snapshot(List.of(), List.of(), List.of());
    private static final LevelTickCache<Snapshot> CACHE = new LevelTickCache();

    private CombatVisualEntities() {
    }

    public static Snapshot get(ClientLevel level) {
        if (level == null) {
            CACHE.clear();
            return EMPTY;
        }
        return CACHE.get(level, level.m_46467_(), () -> CombatVisualEntities.collect(level));
    }

    private static Snapshot collect(ClientLevel level) {
        ArrayList<EasterBossEntity> hiddenBosses = new ArrayList<EasterBossEntity>();
        ArrayList<Entity> orbLinks = new ArrayList<Entity>();
        ArrayList<Entity> flywheels = new ArrayList<Entity>();
        for (Entity entity : level.m_104735_()) {
            if (entity.m_213877_()) continue;
            if (entity instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)entity;
                hiddenBosses.add(boss);
                continue;
            }
            if (entity instanceof DefectCoreEntity || entity instanceof ChargedOrbEntity) {
                orbLinks.add(entity);
                continue;
            }
            if (!(entity instanceof FlywheelProjectileEntity) && !(entity instanceof PlayerMoonChakramEntity)) continue;
            flywheels.add(entity);
        }
        if (hiddenBosses.isEmpty() && orbLinks.isEmpty() && flywheels.isEmpty()) {
            return EMPTY;
        }
        return new Snapshot(List.copyOf(hiddenBosses), List.copyOf(orbLinks), List.copyOf(flywheels));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.m_91087_().f_91073_ == null) {
            CACHE.clear();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        CACHE.clear(event.getLevel());
    }

    public record Snapshot(List<EasterBossEntity> hiddenBosses, List<Entity> orbLinks, List<Entity> flywheels) {
    }
}

