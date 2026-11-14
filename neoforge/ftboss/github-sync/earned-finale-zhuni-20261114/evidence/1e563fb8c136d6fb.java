package net.farmingtales.ftboss.turret;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(
   modid = "ftboss"
)
public final class ZhuniStage {
   private static final String STATE = "ftboss.zhuniStage";
   private static final String BIRD = "ftboss.zhuniId";

   private ZhuniStage() {
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void joined(EntityJoinLevelEvent var0) {
      if (var0.getEntity() instanceof EasterBossEntity var1
         && !var0.getLevel().isClientSide()
         && var1.getVariant() == 6
         && ManorBalanceHooks.mainBoss(var1)
         && ManorFightMembers.fightId(var1) != null
         && ManorFightMembers.ownerId(var1) == null) {
         CompoundTag var3 = var1.getPersistentData();
         if (!var3.contains("ftboss.zhuniStage")) {
            var3.putInt("ftboss.zhuniStage", 1);
            var3.putBoolean("ftboss.zhuniOriginalAi", var1.isNoAi());
            var3.putBoolean("ftboss.zhuniOriginalInvulnerable", var1.isInvulnerable());
            var3.putBoolean("ftboss.zhuniOriginalInvisible", var1.isInvisible());
            var3.putBoolean("ftboss.zhuniOriginalHidden", var1.isManorIntroHidden());
         }

         if (var3.getInt("ftboss.zhuniStage") == 1) {
            hide(var1);
         }

         return;
      }
   }

   private static void hide(EasterBossEntity var0) {
      var0.setNoAi(true);
      var0.setInvulnerable(true);
      var0.setInvisible(true);
      var0.setManorIntroHidden(true);
      syncBossBar(var0);
   }

   public static boolean waiting(EasterBossEntity var0) {
      return var0.getPersistentData().getInt("ftboss.zhuniStage") == 1;
   }

   public static void reveal(EasterBossEntity var0, ZhuniEntity var1) {
      if (waiting(var0) && var0.isAlive()) {
         CompoundTag var2 = var0.getPersistentData();
         var2.putInt("ftboss.zhuniStage", 2);
         var0.setNoAi(var2.getBoolean("ftboss.zhuniOriginalAi"));
         var0.setInvulnerable(var2.getBoolean("ftboss.zhuniOriginalInvulnerable"));
         var0.setInvisible(var2.getBoolean("ftboss.zhuniOriginalInvisible"));
         var0.setManorIntroHidden(var2.getBoolean("ftboss.zhuniOriginalHidden"));
         var0.getBossBar().setVisible(true);
         var1.revealedAt(var0.level().getGameTime());
         if (var0.level() instanceof ServerLevel var3) {
            for (ServerPlayer var5 : ManorBalanceHooks.players(var0, var3)) {
               var5.sendSystemMessage(Component.literal("§a竹鸟振翅！§b玄蓝登场。"));
            }
         }
      }
   }

   public static boolean hold(EasterBossEntity var0) {
      if (var0.level() instanceof ServerLevel var1
         && var0.getVariant() == 6
         && ManorBalanceHooks.mainBoss(var0)
         && var0.isAlive()
         && ManorFightMembers.ownerId(var0) == null) {
         CompoundTag var7 = var0.getPersistentData();
         int var3 = var7.getInt("ftboss.zhuniStage");
         if (var3 == 0
            && ManorFightMembers.fightId(var0) != null
            && ManorFight.hasActive(ManorFightMembers.fightId(var0))
            && !HiddenBossIntro.isActing(var0.getUUID())) {
            var7.putInt("ftboss.zhuniStage", 1);
            var7.putBoolean("ftboss.zhuniOriginalAi", var0.isNoAi());
            var7.putBoolean("ftboss.zhuniOriginalInvulnerable", var0.isInvulnerable());
            var7.putBoolean("ftboss.zhuniOriginalInvisible", var0.isInvisible());
            var7.putBoolean("ftboss.zhuniOriginalHidden", var0.isManorIntroHidden());
            var3 = 1;
         }

         if (var3 != 1 && var3 != 2) {
            return false;
         }

         UUID var4 = ManorFightMembers.fightId(var0);
         if (var4 != null && ManorFight.hasActive(var4)) {
            Entity var5 = var7.hasUUID("ftboss.zhuniId") ? var1.getEntity(var7.getUUID("ftboss.zhuniId")) : null;
            if (!(var5 instanceof ZhuniEntity) || !var0.getUUID().equals(((ZhuniEntity)var5).ownerId())) {
               ZhuniEntity var6 = new ZhuniEntity(XuanlanTurrets.BIRD, var1);
               var6.setOwner(var0);
               var6.moveTo(var0.getX(), var0.getY() + 0.65, var0.getZ(), var0.getYRot(), 0.0F);
               var6.revealedAt(var1.getGameTime());
               ManorFightMembers.inherit(var0, var6);
               if (var1.addFreshEntity(var6)) {
                  var7.putUUID("ftboss.zhuniId", var6.getUUID());
               }
            }

            if (var3 != 1) {
               return false;
            }

            hide(var0);
            return true;
         }

         return false;
      }

      return false;
   }

   public static void syncBossBar(EasterBossEntity var0) {
      if (var0.level() instanceof ServerLevel var1 && var0.isAlive() && !HiddenBossIntro.isActing(var0.getUUID())) {
         ServerBossEvent var7 = var0.getBossBar();
         List var3 = ManorBalanceHooks.players(var0, var1);
         var7.setName(Component.literal("竹鸟 · 玄蓝"));
         float var4 = var0.getMaxHealth();
         var7.setProgress(var4 > 0.0F ? Math.max(0.0F, Math.min(1.0F, var0.getHealth() / var4)) : 0.0F);

         for (ServerPlayer var6 : List.copyOf(var7.getPlayers())) {
            if (!var3.contains(var6)) {
               var7.removePlayer(var6);
            }
         }

         for (ServerPlayer var9 : var3) {
            var7.addPlayer(var9);
         }

         var7.setVisible(true);
         return;
      }
   }
}
