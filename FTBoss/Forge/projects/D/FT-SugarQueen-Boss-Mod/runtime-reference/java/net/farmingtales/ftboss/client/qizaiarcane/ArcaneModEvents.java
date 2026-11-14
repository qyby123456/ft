/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.world.entity.EntityType
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneEntityVisuals;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ArcaneModEvents {
    private ArcaneModEvents() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void entities(EntityRenderersEvent.RegisterRenderers registerRenderers) {
        registerRenderers.registerEntityRenderer((EntityType)FTEntities.QIZAI_STAR.get(), ArcaneEntityVisuals.Impulse::new);
        registerRenderers.registerEntityRenderer((EntityType)FTEntities.QIZAI_SIGIL.get(), ArcaneEntityVisuals.Field::new);
    }

    @SubscribeEvent
    public static void resources(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
        registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)new ArcaneAssets());
    }
}

