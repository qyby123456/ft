package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.farmingtales.ftboss.sound.BossMusicPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class FTNetworkRegistration {
   private static boolean registered;

   private FTNetworkRegistration() {
   }

   public static synchronized void register(IEventBus var0) {
      if (!registered) {
         registered = true;
         var0.addListener(FTNetworkRegistration::onRegisterPayloads);
         FTBossMod.LOGGER.info("[FTBoss][网络] 已挂载 payload 注册监听器 (NeoForge 1.21.1 网络系统)");
      }
   }

   private static void onRegisterPayloads(RegisterPayloadHandlersEvent var0) {
      PayloadRegistrar var1 = var0.registrar("1").optional();
      var1.playToClient(ShadowSigilPacket.TYPE, ShadowSigilPacket.STREAM_CODEC, ShadowSigilPacket::handle);
      var1.playToClient(ManorIntroPacket.TYPE, ManorIntroPacket.STREAM_CODEC, ManorIntroPacket::handle);
      var1.playToClient(ManorRecallBoundaryPacket.TYPE, ManorRecallBoundaryPacket.STREAM_CODEC, ManorRecallBoundaryPacket::handle);
      var1.playToClient(ManorTransitionPacket.TYPE, ManorTransitionPacket.STREAM_CODEC, ManorTransitionPacket::handle);
      var1.playToClient(ManorVictoryPacket.TYPE, ManorVictoryPacket.STREAM_CODEC, ManorVictoryPacket::handle);
      var1.playToClient(SpellVisualPacket.TYPE, SpellVisualPacket.STREAM_CODEC, SpellVisualPacket::handle);
      var1.playToClient(DandanScytheFxPacket.TYPE, DandanScytheFxPacket.STREAM_CODEC, DandanScytheFxPacket::handle);
      var1.playToClient(MaqueFeatherPacket.TYPE, MaqueFeatherPacket.STREAM_CODEC, MaqueFeatherPacket::handle);
      var1.playToClient(BossBarFeedPacket.TYPE, BossBarFeedPacket.STREAM_CODEC, BossBarFeedPacket::handle);
      var1.playToClient(YouziStatuePacket.TYPE, YouziStatuePacket.STREAM_CODEC, YouziStatuePacket::handle);
      var1.playToClient(WeaponFxPacket.TYPE, WeaponFxPacket.STREAM_CODEC, WeaponFxPacket::handle);
      var1.playToClient(LemonCatFxPacket.TYPE, LemonCatFxPacket.STREAM_CODEC, LemonCatFxPacket::handle);
      var1.playToClient(LemonSacrificePacket.TYPE, LemonSacrificePacket.STREAM_CODEC, LemonSacrificePacket::handle);
      var1.playToClient(XuanlanFxPacket.TYPE, XuanlanFxPacket.STREAM_CODEC, XuanlanFxPacket::handle);
      var1.playToClient(BossMusicPacket.TYPE, BossMusicPacket.STREAM_CODEC, BossMusicPacket::handle);
      var1.playToServer(NonManorWeaponPacket.TYPE, NonManorWeaponPacket.STREAM_CODEC, NonManorWeaponPacket::handle);
   }
}
