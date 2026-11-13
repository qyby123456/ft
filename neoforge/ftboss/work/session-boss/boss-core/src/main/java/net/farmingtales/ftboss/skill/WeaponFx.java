package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.boss.ai.BossHitFeedback;
import net.farmingtales.ftboss.api.OptionalWeaponModule;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "ftboss",
   bus = Bus.GAME
)
public final class WeaponFx {
   private static final String LAST_HIT = "ftboss:weapon_fx_hit";

   private WeaponFx() {
   }

   public static void burst(ServerLevel var0, Vec3 var1, double var2, Theme var4) {
      send(var0, Kind.BLAST, var4, var1, var1, var2, 0.0F);
   }

   public static void ring(ServerLevel var0, Vec3 var1, double var2, Theme var4) {
      send(var0, Kind.RING, var4, var1, var1, var2, 0.0F);
   }

   public static void beam(ServerLevel var0, Vec3 var1, Vec3 var2, Theme var3) {
      send(var0, Kind.BEAM, var3, var1, var2, 0.15, 0.0F);
   }

   public static void send(ServerLevel var0, Kind var1, Theme var2, Vec3 var3, Vec3 var4, double var5, float var7) {
      WeaponFxPacket var8 = new WeaponFxPacket(var0.dimension().location(), var1, var2, var3, var4, (float)var5, var7);
      if (var8.valid()) {
         Vec3 var9 = var4.subtract(var3);
         double var10 = var9.lengthSqr();
         double var12 = 48.0 + var5;

         for (ServerPlayer var15 : var0.players()) {
            double var16 = var10 < 1.0E-8 ? 0.0 : Math.max(0.0, Math.min(1.0, var15.position().subtract(var3).dot(var9) / var10));
            if (var15.position().distanceToSqr(var3.add(var9.scale(var16))) <= var12 * var12 && var15.connection.hasChannel(WeaponFxPacket.TYPE)) {
               PacketDistributor.sendToPlayer(var15, var8, new CustomPacketPayload[0]);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void hit(LivingIncomingDamageEvent var0) {
      if (!(var0.getAmount() <= 0.0F)
         && var0.getSource().getDirectEntity() instanceof ServerPlayer var1
         && var0.getSource().is(DamageTypes.PLAYER_ATTACK)
         && !var1.isSpectator()) {
         ItemStack var14 = var1.getMainHandItem();
         ResourceLocation var3 = BuiltInRegistries.ITEM.getKey(var14.getItem());
         String var4 = var3 == null ? null : var3.toString();
         boolean var5 = "ftboss:gourmet_blank_blade".equals(var4);
         if (var5 || !BossHitFeedback.supports(var0.getEntity())) {
            if (!WeaponFxRules.isDoraGunItem(var4)) {
               Theme var6 = WeaponFxRules.forItem(var4);
               if (var6 != null) {
                  long var7 = var1.serverLevel().getGameTime();
                  CompoundTag var9 = var1.getPersistentData();
                  if (!var9.contains("ftboss:weapon_fx_hit")
                     || var7 < var9.getLong("ftboss:weapon_fx_hit")
                     || var7 - var9.getLong("ftboss:weapon_fx_hit") >= 6L) {
                     var9.putLong("ftboss:weapon_fx_hit", var7);
                     Vec3 var10 = var0.getEntity().getBoundingBox().getCenter();
                     Vec3 var11 = var1.getEyePosition().subtract(var10).normalize();
                     var10 = var10.add(var11.scale(Math.min(0.6, (double)var0.getEntity().getBbWidth() * 0.5)));
                     double var12 = var5
                        ? 0.72
                           + (double)Math.min(8, OptionalWeaponModule.evolutionLevel(var14)) * 0.035
                           + (double)Math.min(30, OptionalWeaponModule.socketCount(var14)) * 0.008
                        : 0.65;
                     send(var1.serverLevel(), var5 ? Kind.GOURMET_HIT : Kind.HIT, var6, var10, var10, var12, var1.getYRot());
                  }
               }
            }
         }
      }
   }
}
