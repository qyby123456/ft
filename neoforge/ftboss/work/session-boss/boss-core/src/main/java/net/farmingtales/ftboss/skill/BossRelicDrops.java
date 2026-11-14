package net.farmingtales.ftboss.skill;

import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.item.BossRelicItem;
import net.farmingtales.ftboss.item.BossRelicItems;
import net.farmingtales.ftboss.item.BossMaterialDrops;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class BossRelicDrops {
   private static final String REWARDED = "ftboss:boss_relic_rewarded";

   private BossRelicDrops() {
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void scriptedDeath(LivingDeathEvent var0) {
      LivingEntity var1 = var0.getEntity();
      if (var1 instanceof SugarQueenEntity || var1 instanceof MeatMountainEntity) {
         reward(var1, var0.getSource(), var1x -> {
            ItemEntity var2 = new ItemEntity(var1.level(), var1.getX(), var1.getY() + 0.5, var1.getZ(), var1x);
            var2.setDefaultPickUpDelay();
            var1.level().addFreshEntity(var2);
         });
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void normalDrops(LivingDropsEvent var0) {
      LivingEntity var1 = var0.getEntity();
      if (!(var1 instanceof SugarQueenEntity) && !(var1 instanceof MeatMountainEntity)) {
         reward(var1, var0.getSource(), var2 -> {
            ItemEntity var3 = new ItemEntity(var1.level(), var1.getX(), var1.getY() + 0.5, var1.getZ(), var2);
            var3.setDefaultPickUpDelay();
            var0.getDrops().add(var3);
         });
      }
   }

   private static void reward(LivingEntity var0, DamageSource var1, Consumer<ItemStack> var2) {
      if (!LemonSummonRewards.suppressed(var0)) {
         String var3 = EntityType.getKey(var0.getType()).toString();
         if (var0.level() instanceof ServerLevel var4) {
            if (!var4.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
               FTBossMod.LOGGER.info("[FTBoss] 遗物跳过: {} —— mobLoot 游戏规则关闭", var3);
            } else if (var0.getPersistentData().getBoolean("ftboss:boss_relic_rewarded")) {
               FTBossMod.LOGGER.info("[FTBoss] 遗物跳过: {} —— 这只已经结算过(REWARDED)", var3);
            } else if (!(var1.getEntity() instanceof ServerPlayer) && !(var0.getKillCredit() instanceof ServerPlayer)) {
               FTBossMod.LOGGER.info("[FTBoss] 遗物跳过: {} —— 击杀者不是玩家（source={}, killCredit={}）", new Object[]{var3, var1.getEntity(), var0.getKillCredit()});
            } else {
               boolean var10001;
               label41: {
                  if (var0 instanceof RatBossEntity var6 && var6.isMinion()) {
                     var10001 = true;
                     break label41;
                  }

                  var10001 = false;
               }

               Kind var8 = BossRelicRules.forEntity(var3, var10001);
               if (var8 != null) {
                  var0.getPersistentData().putBoolean("ftboss:boss_relic_rewarded", true);
                  BossMaterialDrops.reward(var8, var2);
                  float var9 = var0.getRandom().nextFloat();
                  if (BossRelicRules.drops(var9)) {
                     ItemStack var7 = BossRelicItem.roll(BossRelicItems.get(var8), var0.getRandom());
                     var2.accept(var7);
                     FTBossMod.LOGGER
                        .info(
                           "[FTBoss] 遗物掉落: {}（{}，掷点 {}=命中 {}%）",
                           new Object[]{var7.getHoverName().getString(), var3, String.format("%.3f", var9), String.format("%.0f", 100.0F)}
                        );
                  } else {
                     FTBossMod.LOGGER.info("[FTBoss] 遗物未掉: {}（{}，掷点 {}≥{}，本次没中）", new Object[]{var8.item, var3, String.format("%.3f", var9), 1.0F});
                  }

                  if (var8 == Kind.FROST) {
                     var2.accept(new ItemStack((ItemLike)BossRelicItems.FROST_QUEEN_STAFF.get()));
                  }
               }
            }
         }
      }
   }
}
