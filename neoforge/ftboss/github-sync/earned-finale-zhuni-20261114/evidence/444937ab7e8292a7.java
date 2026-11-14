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
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
         && !var0.getLevel().m_5776_()
         && var1.getVariant() == 6
         && ManorBalanceHooks.mainBoss(var1)
         && ManorFightMembers.fightId(var1) != null
         && ManorFightMembers.ownerId(var1) == null) {
         CompoundTag var3 = var1.getPersistentData();
         if (!var3.m_128441_("ftboss.zhuniStage")) {
            var3.m_128405_("ftboss.zhuniStage", 1);
            var3.m_128379_("ftboss.zhuniOriginalAi", var1.m_21525_());
            var3.m_128379_("ftboss.zhuniOriginalInvulnerable", var1.m_20147_());
            var3.m_128379_("ftboss.zhuniOriginalInvisible", var1.m_20145_());
            var3.m_128379_("ftboss.zhuniOriginalHidden", var1.isManorIntroHidden());
         }

         if (var3.m_128451_("ftboss.zhuniStage") == 1) {
            hide(var1);
         }

         return;
      }
   }

   private static void hide(EasterBossEntity var0) {
      var0.m_21557_(true);
      var0.m_20331_(true);
      var0.m_6842_(true);
      var0.setManorIntroHidden(true);
      syncBossBar(var0);
   }

   public static boolean waiting(EasterBossEntity var0) {
      return var0.getPersistentData().m_128451_("ftboss.zhuniStage") == 1;
   }

   public static void reveal(EasterBossEntity var0, ZhuniEntity var1) {
      if (waiting(var0) && var0.m_6084_()) {
         CompoundTag var2 = var0.getPersistentData();
         var2.m_128405_("ftboss.zhuniStage", 2);
         var0.m_21557_(var2.m_128471_("ftboss.zhuniOriginalAi"));
         var0.m_20331_(var2.m_128471_("ftboss.zhuniOriginalInvulnerable"));
         var0.m_6842_(var2.m_128471_("ftboss.zhuniOriginalInvisible"));
         var0.setManorIntroHidden(var2.m_128471_("ftboss.zhuniOriginalHidden"));
         var0.getBossBar().m_8321_(true);
         var1.revealedAt(var0.m_9236_().m_46467_());
         if (var0.m_9236_() instanceof ServerLevel var3) {
            for (ServerPlayer var5 : ManorBalanceHooks.players(var0, var3)) {
               var5.m_213846_(Component.m_237113_("§a竹鸟振翅！§b玄蓝登场。"));
            }
         }
      }
   }

   public static boolean hold(EasterBossEntity var0) {
      if (var0.m_9236_() instanceof ServerLevel var1
         && var0.getVariant() == 6
         && ManorBalanceHooks.mainBoss(var0)
         && var0.m_6084_()
         && ManorFightMembers.ownerId(var0) == null) {
         CompoundTag var7 = var0.getPersistentData();
         int var3 = var7.m_128451_("ftboss.zhuniStage");
         if (var3 == 0
            && ManorFightMembers.fightId(var0) != null
            && ManorFight.hasActive(ManorFightMembers.fightId(var0))
            && !HiddenBossIntro.isActing(var0.m_20148_())) {
            var7.m_128405_("ftboss.zhuniStage", 1);
            var7.m_128379_("ftboss.zhuniOriginalAi", var0.m_21525_());
            var7.m_128379_("ftboss.zhuniOriginalInvulnerable", var0.m_20147_());
            var7.m_128379_("ftboss.zhuniOriginalInvisible", var0.m_20145_());
            var7.m_128379_("ftboss.zhuniOriginalHidden", var0.isManorIntroHidden());
            var3 = 1;
         }

         if (var3 != 1 && var3 != 2) {
            return false;
         }

         UUID var4 = ManorFightMembers.fightId(var0);
         if (var4 != null && ManorFight.hasActive(var4)) {
            Entity var5 = var7.m_128403_("ftboss.zhuniId") ? var1.m_8791_(var7.m_128342_("ftboss.zhuniId")) : null;
            if (!(var5 instanceof ZhuniEntity) || !var0.m_20148_().equals(((ZhuniEntity)var5).ownerId())) {
               ZhuniEntity var6 = new ZhuniEntity(XuanlanTurrets.BIRD, var1);
               var6.setOwner(var0);
               var6.m_7678_(var0.m_20185_(), var0.m_20186_() + 0.65, var0.m_20189_(), var0.m_146908_(), 0.0F);
               var6.revealedAt(var1.m_46467_());
               ManorFightMembers.inherit(var0, var6);
               if (var1.m_7967_(var6)) {
                  var7.m_128362_("ftboss.zhuniId", var6.m_20148_());
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
      if (var0.m_9236_() instanceof ServerLevel var1 && var0.m_6084_() && !HiddenBossIntro.isActing(var0.m_20148_())) {
         ServerBossEvent var7 = var0.getBossBar();
         List var3 = ManorBalanceHooks.players(var0, var1);
         var7.m_6456_(Component.m_237113_("竹鸟 · 玄蓝"));
         float var4 = var0.m_21233_();
         var7.m_142711_(var4 > 0.0F ? Math.max(0.0F, Math.min(1.0F, var0.m_21223_() / var4)) : 0.0F);

         for (ServerPlayer var6 : List.copyOf(var7.m_8324_())) {
            if (!var3.contains(var6)) {
               var7.m_6539_(var6);
            }
         }

         for (ServerPlayer var9 : var3) {
            var7.m_6543_(var9);
         }

         var7.m_8321_(true);
         return;
      }
   }
}
