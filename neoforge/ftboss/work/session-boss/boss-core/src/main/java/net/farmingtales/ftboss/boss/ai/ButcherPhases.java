package net.farmingtales.ftboss.boss.ai;

import net.minecraft.resources.ResourceLocation;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class ButcherPhases {
   private static final String PHASE = "ftboss_butcher_phase";
   private static final String ELAPSED = "ftboss_butcher_phase_ticks";
   private static final String DODGE_CD = "ftboss_butcher_dodge_cd";
   private static final String BYPASS = "ftboss_butcher_phase_bypass";
   private static final ResourceLocation HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath("ftboss", "butcher_phase_health");

   private ButcherPhases() {
   }

   public static int phase(MeatMountainEntity var0) {
      return ButcherPhaseRules.normalize(var0.getPersistentData().getInt("ftboss_butcher_phase"));
   }

   public static int remainingSeconds(MeatMountainEntity var0) {
      int var1 = Math.max(0, var0.getPersistentData().getInt("ftboss_butcher_phase_ticks"));
      return Math.max(0, (1200 - var1 + 19) / 20);
   }

   public static void tick(MeatMountainEntity var0) {
      if (!var0.level().isClientSide && var0.isAlive()) {
         CompoundTag var1 = var0.getPersistentData();
         if (!var1.contains("ftboss_butcher_phase")) {
            var1.putInt("ftboss_butcher_phase", 1);
            BossRage.reset(var0);
            var0.removeAllEffects();
         }

         applyHealth(var0);
         int var2 = var1.getInt("ftboss_butcher_dodge_cd");
         if (var2 > 0) {
            var1.putInt("ftboss_butcher_dodge_cd", var2 - 1);
         }

         if (phase(var0) == 3) {
            int var3 = Math.min(1200, Math.max(0, var1.getInt("ftboss_butcher_phase_ticks"))) + 1;
            var1.putInt("ftboss_butcher_phase_ticks", var3);
            if (ButcherPhaseRules.expired(phase(var0), var3)) {
               enter(var0, 1);
            } else {
               float var4 = ButcherPhaseRules.regeneration(phase(var0), var3, var0.getMaxHealth(), var0.getMaxHealth() - var0.getHealth());
               if (var4 > 0.0F) {
                  var0.heal(var4);
                  if (var0.level() instanceof ServerLevel var5) {
                     var5.sendParticles(ParticleTypes.HEART, var0.getX(), var0.getY() + 1.5, var0.getZ(), 3, 0.4, 0.4, 0.4, 0.0);
                  }
               }
            }
         }
      }
   }

   public static boolean advance(MeatMountainEntity var0) {
      if (var0.getPersistentData().getBoolean("ftboss_butcher_phase_bypass")) {
         return false;
      } else {
         int var1 = ButcherPhaseRules.afterDefeat(phase(var0));
         if (var1 == 0) {
            return false;
         } else {
            enter(var0, var1);
            return true;
         }
      }
   }

   private static void enter(MeatMountainEntity var0, int var1) {
      CompoundTag var2 = var0.getPersistentData();
      var2.putInt("ftboss_butcher_phase", var1);
      var2.putInt("ftboss_butcher_phase_ticks", 0);
      var2.putInt("ftboss_butcher_dodge_cd", 0);
      BossRage.reset(var0);
      var0.removeAllEffects();
      applyHealth(var0);
      var0.setHealth(var0.getMaxHealth());
      var0.invulnerableTime = 0;
      var0.resetButcherCombat();
      MeatBoss.onPhaseChanged(var0, var1);
      if (var0.level() instanceof ServerLevel var3) {
         var3.playSound(null, var0.blockPosition(), SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.0F, 0.8F);
      }

      FTBossMod.LOGGER.info("[FTBoss] 屠夫阶段={} 血量={} 三阶段剩余={}秒 实体={}", new Object[]{var1, var0.getMaxHealth(), var1 == 3 ? 60 : 0, var0.getUUID()});
   }

   private static void applyHealth(MeatMountainEntity var0) {
      AttributeInstance var1 = var0.getAttribute(Attributes.MAX_HEALTH);
      if (var1 != null) {
         double var2 = ButcherPhaseRules.healthMultiplier(phase(var0)) - 1.0;
         AttributeModifier var4 = var1.getModifier(HEALTH_MODIFIER);
         if (var4 == null || var4.amount() != var2) {
            var1.removeModifier(HEALTH_MODIFIER);
            if (var2 != 0.0) {
               var1.addTransientModifier(new AttributeModifier(HEALTH_MODIFIER, var2, Operation.ADD_MULTIPLIED_TOTAL));
            }

            if (var0.getHealth() > var0.getMaxHealth()) {
               var0.setHealth(var0.getMaxHealth());
            }
         }
      }
   }

   public static void beginDamage(MeatMountainEntity var0, DamageSource var1) {
      var0.getPersistentData().putBoolean("ftboss_butcher_phase_bypass", var1.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
   }

   public static void endDamage(MeatMountainEntity var0) {
      var0.getPersistentData().remove("ftboss_butcher_phase_bypass");
   }

   public static boolean dodge(MeatMountainEntity var0, DamageSource var1) {
      if (var0.isAlive() && !var0.isInvulnerableTo(var1) && var1.getEntity() != null && var1.getEntity() != var0 && !var1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         CompoundTag var2 = var0.getPersistentData();
         if (phase(var0) == 3 && var2.getInt("ftboss_butcher_dodge_cd") <= 0) {
            if (!ButcherPhaseRules.dodge(phase(var0), var2.getInt("ftboss_butcher_dodge_cd"), var0.getRandom().nextFloat())) {
               return false;
            } else {
               var2.putInt("ftboss_butcher_dodge_cd", 20);
               if (var0.level() instanceof ServerLevel var3) {
                  var3.sendParticles(ParticleTypes.CLOUD, var0.getX(), var0.getY() + 1.0, var0.getZ(), 8, 0.5, 0.6, 0.5, 0.04);
                  var3.playSound(null, var0.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.6F, 1.4F);
               }

               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static float adjustScaledHealth(LivingEntity var0, float var1) {
      if (!(var0 instanceof MeatMountainEntity)) {
         return var1;
      } else {
         AttributeInstance var2 = var0.getAttribute(Attributes.MAX_HEALTH);
         return var2 != null && !(var2.getBaseValue() <= 0.0) ? ButcherPhaseRules.scaledHealth(var1 / (float)var2.getBaseValue(), var0.getMaxHealth()) : var1;
      }
   }
}
