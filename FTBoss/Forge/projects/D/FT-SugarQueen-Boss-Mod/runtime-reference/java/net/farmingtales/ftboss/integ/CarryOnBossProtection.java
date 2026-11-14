/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.Event
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 */
package net.farmingtales.ftboss.integ;

import java.lang.reflect.Field;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CarryOnBossProtection {
    public static final TagKey<EntityType<?>> BLACKLIST = TagKey.m_203882_((ResourceKey)Registries.f_256939_, (ResourceLocation)new ResourceLocation("ftboss", "carryon_blacklist"));
    private static boolean registered;

    private CarryOnBossProtection() {
    }

    public static boolean isProtected(Entity target) {
        return target != null && target.m_6095_().m_204039_(BLACKLIST);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("carryon")) {
            event.enqueueWork(CarryOnBossProtection::register);
        }
    }

    private static void register() {
        if (registered) {
            return;
        }
        try {
            Class<Event> eventType = Class.forName("tschipp.carryon.events.EntityPickupEvent").asSubclass(Event.class);
            Field target = eventType.getField("target");
            if (!Entity.class.isAssignableFrom(target.getType())) {
                throw new IllegalStateException("Unsupported Carry On target field");
            }
            CarryOnBossProtection.registerListener(eventType, target);
            registered = true;
            FTBossMod.LOGGER.info("[FTBoss][CarryOn] Boss pickup protection registered");
        }
        catch (ReflectiveOperationException | RuntimeException failure) {
            FTBossMod.LOGGER.error("[FTBoss][CarryOn] Cannot register boss pickup protection", (Throwable)failure);
        }
    }

    private static <PickupEvent extends Event> void registerListener(Class<PickupEvent> eventType, Field target) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, eventType, event -> {
            try {
                if (CarryOnBossProtection.isProtected((Entity)target.get(event))) {
                    event.setCanceled(true);
                }
            }
            catch (IllegalAccessException failure) {
                throw new IllegalStateException("Cannot read Carry On pickup target", failure);
            }
        });
    }
}

