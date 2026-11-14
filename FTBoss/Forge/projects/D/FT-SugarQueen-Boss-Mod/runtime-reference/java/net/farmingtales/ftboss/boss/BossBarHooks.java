/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 */
package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.boss.BossBarFeed;
import net.farmingtales.ftboss.network.BossBarNetwork;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class BossBarHooks {
    private BossBarHooks() {
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            BossBarNetwork.register();
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ServerEvents {
        private ServerEvents() {
        }

        @SubscribeEvent
        public static void serverTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                BossBarFeed.tick(event.getServer());
            }
        }

        @SubscribeEvent
        public static void serverStopped(ServerStoppedEvent event) {
            BossBarFeed.reset();
        }
    }
}

