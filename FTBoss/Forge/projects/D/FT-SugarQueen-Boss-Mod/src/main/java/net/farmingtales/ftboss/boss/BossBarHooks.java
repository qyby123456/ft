package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.network.BossBarNetwork;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class BossBarHooks {
   private BossBarHooks() {
   }

   @EventBusSubscriber(
      modid = "ftboss",
      bus = Bus.FORGE
   )
   public static final class ServerEvents {
      private ServerEvents() {
      }

      @SubscribeEvent
      public static void serverTick(ServerTickEvent event) {
         if (event.phase == Phase.END) {
            BossBarFeed.tick(event.getServer());
         }
      }

      @SubscribeEvent
      public static void serverStopped(ServerStoppedEvent event) {
         BossBarFeed.reset();
      }
   }

   @EventBusSubscriber(
      modid = "ftboss",
      bus = Bus.MOD
   )
   public static final class Setup {
      private Setup() {
      }

      @SubscribeEvent
      public static void commonSetup(FMLCommonSetupEvent event) {
         BossBarNetwork.register();
      }
   }
}
