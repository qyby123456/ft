package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class GongdiCastLifecycle {
   private GongdiCastLifecycle() {
   }

   public static boolean canBegin(EasterBossEntity var0) {
      return var0.getVariant() == 3 && var0.level() instanceof ServerLevel && var0.isAlive() && !var0.isMini() && !var0.isNoAi();
   }

   public static boolean canStart(EasterBossEntity var0, ServerLevel var1, LivingEntity var2) {
      return canBegin(var0) && var0.level() == var1 && var2 != null && ManorLivingTargets.valid(var0, var2);
   }

   public static boolean canFinish(EasterBossEntity var0) {
      CompoundTag var1 = var0.getSkillCue();
      return canBegin(var0) && var1.contains("gongdiSkill") && !var1.getBoolean("released");
   }

   public static void cancel(EasterBossEntity var0) {
      if (var0.getVariant() == 3
         && var0.level() instanceof ServerLevel
         && var0.getSkillCue().contains("gongdiSkill")
         && !var0.getSkillCue().getBoolean("released")) {
         var0.cancelManorFiveCast();
      }
   }
}
