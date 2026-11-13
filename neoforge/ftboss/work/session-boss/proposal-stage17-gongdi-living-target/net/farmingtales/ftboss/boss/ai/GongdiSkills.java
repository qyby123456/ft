package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.farmingtales.ftboss.util.GongdiCombatRules.Bleed;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class GongdiSkills {
   private static final ResourceKey<DamageType> GUILLOTINE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "gongdi_guillotine"));
   private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.8F, 0.06F, 0.12F), 1.1F);
   private static final int ROT_GUILLOTINE = 0;
   private static final int ROT_STRIKE = 1;
   private static final int ROT_PULL = 2;
   private static final int[] ROTATION = new int[]{0, 1, 2};
   private final EasterBossEntity boss;
   private final Map<UUID, GongdiSkills.BleedingTarget> bleeding = new HashMap<>();
   private String skill = "";
   private int windup;
   private int rotation;
   private LivingEntity lockedTarget;
   private Vec3 direction = Vec3.ZERO;
   private long strikeReadyAt;
   private long pullReadyAt;
   private long executeReadyAt;
   private final GongdiGuillotineCooldown guillotineResetWindow;

   public GongdiSkills(EasterBossEntity var1) {
      this.boss = var1;
      this.guillotineResetWindow = new GongdiGuillotineCooldown();
   }

   public boolean isCasting() {
      return this.windup > 0;
   }

   public void reset() {
      GongdiCastLifecycle.cancel(this.boss);
      this.windup = 0;
      this.skill = "";
      this.lockedTarget = null;
      this.bleeding.clear();
      this.rotation = 0;
      this.strikeReadyAt = 0L;
      this.pullReadyAt = 0L;
      this.executeReadyAt = 0L;
      this.guillotineResetWindow.clear();
   }

   public boolean validTarget(LivingEntity var1, double var2) {
      if (!ManorLivingTargets.valid(this.boss, var1)) {
         return false;
      } else {
         Vec3 var4 = var1.position().subtract(this.boss.position());
         return var1.level() == this.boss.level()
            && var1.isAlive()
            && !ManorLivingTargets.creative(var1)
            && !var1.isSpectator()
            && GongdiCombatRules.inRange(var4.x, var4.y, var4.z, var2)
            && this.boss.hasLineOfSight(var1);
      }
   }

   public void onHit(LivingEntity var1) {
      if (var1.isAlive() && !ManorLivingTargets.creative(var1) && !var1.isSpectator()) {
         long var2 = this.boss.level().getGameTime();
         GongdiSkills.BleedingTarget var4 = this.bleeding.get(var1.getUUID());
         if (var4 == null || var4.player() != var1) {
            var4 = new GongdiSkills.BleedingTarget(var1, new Bleed());
            this.bleeding.put(var1.getUUID(), var4);
         }

         int var5 = var4.bleed().addHit(var2, this.boss.isGongdiBloodRageFull());
         if (var1 instanceof Player player) player.displayClientMessage(Component.literal("§c出血 " + var5 + "/5 §7· 5 秒；远离斧刃，提防断头台"), true);
      }
   }

   public void onAbilityHit(ServerLevel var1, LivingEntity var2, float var3) {
      this.boss.onGongdiAbilityHit(var1, var2, var3);
      this.onHit(var2);
   }

   public boolean tryStart(ServerLevel var1, LivingEntity var2) {
      if (this.windup <= 0 && GongdiCastLifecycle.canStart(this.boss, var1, var2) && this.validTarget(var2, 16.875)) {
         long var3 = var1.getGameTime();
         int var5 = this.stacks(var2, var3);
         if (var3 < GongdiGuillotineCooldown.eligibleAt(this.executeReadyAt, this.guillotineResetWindow, var3)
            || !this.validTarget(var2, 10.125)
            || var5 < 3 && !(var2.getHealth() + var2.getAbsorptionAmount() <= GongdiCombatRules.executeDamage(this.boss.gongdiMeleeDamage(var2), var5))) {
            for (int var6 = 0; var6 < ROTATION.length; var6++) {
               int var7 = ROTATION[Math.floorMod(this.rotation, ROTATION.length)];
               this.rotation = (Math.floorMod(this.rotation, ROTATION.length) + 1) % ROTATION.length;
               if (var7 == 0
                  && var3 >= GongdiGuillotineCooldown.eligibleAt(this.executeReadyAt, this.guillotineResetWindow, var3)
                  && this.validTarget(var2, 10.125)) {
                  this.start(var1, var2, "guillotine", GongdiCombatRules.EXECUTE_WINDUP, 10.125);
                  this.executeReadyAt = GongdiGuillotineCooldown.triggered(var3 + 600L, this.guillotineResetWindow);
                  return true;
               }

               if (var7 == 1 && var3 >= this.strikeReadyAt && this.validTarget(var2, 7.65)) {
                  this.start(var1, var2, "crippling_strike", GongdiCombatRules.STRIKE_WINDUP, 7.65);
                  this.strikeReadyAt = var3 + 100L;
                  return true;
               }

               if (var7 == 2 && var3 >= this.pullReadyAt && this.validTarget(var2, 15.75)) {
                  this.start(var1, var2, "apprehend", GongdiCombatRules.PULL_WINDUP, 15.75);
                  this.pullReadyAt = var3 + 160L;
                  return true;
               }
            }

            return false;
         } else {
            this.start(var1, var2, "guillotine", GongdiCombatRules.EXECUTE_WINDUP, 10.125);
            this.executeReadyAt = GongdiGuillotineCooldown.triggered(var3 + 600L, this.guillotineResetWindow);
            return true;
         }
      } else {
         return false;
      }
   }

   private void start(ServerLevel var1, LivingEntity var2, String var3, int var4, double var5) {
      var4 = this.boss.getChallengeDifficulty().acceleratedTicks(var4);
      this.skill = var3;
      this.windup = var4;
      this.lockedTarget = var2;
      Vec3 var7 = var2.position().subtract(this.boss.position());
      this.direction = new Vec3(var7.x, 0.0, var7.z).normalize();
      if (this.direction.lengthSqr() < 0.001) {
         Vec3 var8 = this.boss.getForward();
         this.direction = new Vec3(var8.x, 0.0, var8.z).normalize();
         if (this.direction.lengthSqr() < 0.001) {
            this.direction = new Vec3(0.0, 0.0, 1.0);
         }
      }

      this.boss.setYRot((float)Math.toDegrees(Math.atan2(-this.direction.x, this.direction.z)));
      this.boss.setYBodyRot(this.boss.getYRot());
      this.boss.beginGongdiCast(var3, var4, var5, this.direction);

      String var12 = switch (var3) {
         case "apprehend" -> "无情铁手 · 侧移躲开拉回";
         case "crippling_strike" -> "致残打击 · 后撤躲开重击";
         default -> "诺克萨斯断头台 · 拉开距离或寻找掩体";
      };
      if (var2 instanceof Player player) player.displayClientMessage(Component.literal("§4功底：" + var12), true);
      var1.playSound(null, this.boss.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.0F, 0.7F);
      FTBossMod.LOGGER.info("[FTBoss][Gongdi] {} windup={} target={}", new Object[]{var3, var4, var2.getScoreboardName()});
   }

   public void tick(ServerLevel var1) {
      if (this.boss.level() == var1) {
         if (!GongdiCastLifecycle.canBegin(this.boss)) {
            this.reset();
         } else {
            long var2 = var1.getGameTime();
            Iterator var4 = this.bleeding.values().iterator();

            while (var4.hasNext()) {
               GongdiSkills.BleedingTarget var5 = (GongdiSkills.BleedingTarget)var4.next();
               LivingEntity var6 = var5.player();
               if (ManorLivingTargets.resolve(var1, var6.getUUID()) == var6
                  && var6.isAlive()
                  && !ManorLivingTargets.creative(var6)
                  && !var6.isSpectator()
                  && !(var6.distanceToSqr(this.boss) > 4096.0)) {
                  float var7 = var5.bleed().pulse(var2);
                  if (var7 > 0.0F) {
                     var6.hurt(this.boss.damageSources().mobAttack(this.boss), var7);
                     if (!this.boss.isAlive() || this.boss.isNoAi()) {
                        this.reset();
                        return;
                     }

                     var1.sendParticles(BLOOD, var6.getX(), var6.getY() + 1.1, var6.getZ(), var5.bleed().stacks(var2) + 2, 0.2, 0.4, 0.2, 0.0);
                  }

                  if (var5.bleed().expired(var2)) {
                     var4.remove();
                  }
               } else {
                  var4.remove();
               }
            }

            if (this.windup > 0) {
               if (ManorLivingTargets.valid(this.boss, this.lockedTarget)
                  && this.boss.getSkillCue().getString("gongdiSkill").equals(this.skill)
                  && !this.boss.getSkillCue().getBoolean("released")) {
                  if (!ManorKiting.enabled(this.boss)) {
                     this.boss.getNavigation().stop();
                  }

                  this.boss.setYRot((float)Math.toDegrees(Math.atan2(-this.direction.x, this.direction.z)));
                  this.boss.setYBodyRot(this.boss.getYRot());
                  if (this.skill.equals("guillotine") && this.windup % 4 == 0) {
                     var1.sendParticles(
                        BLOOD, this.lockedTarget.getX(), this.lockedTarget.getY() + 2.2, this.lockedTarget.getZ(), 5, 0.18, 0.3, 0.18, 0.0
                     );
                  }

                  if (--this.windup <= 0) {
                     String var11 = this.skill;
                     this.skill = "";
                     this.boss.finishGongdiCast();

                     try {
                        switch (var11) {
                           case "apprehend":
                              this.pull(var1);
                              break;
                           case "crippling_strike":
                              this.strike(var1);
                              break;
                           case "guillotine":
                              this.execute(var1);
                              break;
                           default:
                              throw new IllegalStateException("Unknown Gongdi skill: " + var11);
                        }
                     } finally {
                        this.lockedTarget = null;
                     }
                  }
               } else {
                  GongdiCastLifecycle.cancel(this.boss);
                  this.windup = 0;
                  this.skill = "";
                  this.lockedTarget = null;
               }
            }
         }
      }
   }

   private boolean inCone(LivingEntity var1, double var2) {
      Vec3 var4 = var1.position().subtract(this.boss.position());
      return GongdiCombatRules.inCone(var4.x, var4.z, this.direction.x, this.direction.z, var2);
   }

   private void pull(ServerLevel var1) {
      for (LivingEntity var3 : ManorLivingTargets.nearby(var1, this.boss)) {
         if (this.validTarget(var3, 15.75) && this.inCone(var3, GongdiCombatRules.PULL_HALF_ANGLE)) {
            Vec3 var4 = this.boss.position().subtract(var3.position());
            double var5 = Math.hypot(var4.x, var4.z);
            if (!(var5 <= 1.8)) {
               Vec3 var7 = new Vec3(var4.x, 0.0, var4.z).normalize().scale(Math.min(1.15, (var5 - 1.8) * 0.32));
               var3.setDeltaMovement(var7.x, 0.18, var7.z);
               var3.hurtMarked = true;
               var3.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, 1));
            }
         }
      }

      var1.playSound(null, this.boss.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.5F, 0.6F);
   }

   private void strike(ServerLevel var1) {
      LivingEntity var2 = this.lockedTarget;
      if (var2 != null && this.validTarget(var2, 7.65) && this.inCone(var2, GongdiCombatRules.STRIKE_HALF_ANGLE)) {
         float var3 = var2.getHealth();
         var2.invulnerableTime = 0;
         float var4 = GongdiCombatRules.strikeDamage(this.boss.gongdiMeleeDamage(var2));
         if (var2.hurt(this.boss.damageSources().mobAttack(this.boss), var4)) {
            this.onAbilityHit(var1, var2, Math.max(0.0F, var3 - var2.getHealth()));
            if (var2.isAlive()) {
               var2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3));
            }

            var1.sendParticles(ParticleTypes.CRIT, var2.getX(), var2.getY() + 1.0, var2.getZ(), 16, 0.3, 0.4, 0.3, 0.08);
            var1.playSound(null, this.boss.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.3F, 0.65F);
         }
      }
   }

   private void execute(ServerLevel var1) {
      LivingEntity var2 = this.lockedTarget;
      if (var2 != null && this.validTarget(var2, 10.125)) {
         int var3 = this.stacks(var2, var1.getGameTime());
         DamageSource var4 = new DamageSource(var1.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(GUILLOTINE), this.boss, this.boss);
         var2.invulnerableTime = 0;
         float var5 = GongdiCombatRules.executeDamage(this.boss.gongdiMeleeDamage(var2), var3);
         if (var2.hurt(var4, var5)) {
            var1.sendParticles(ParticleTypes.SWEEP_ATTACK, var2.getX(), var2.getY() + 1.0, var2.getZ(), 4, 0.2, 0.5, 0.2, 0.0);
            var1.playSound(null, var2.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 0.7F);
            if (!var2.isAlive() && var2 instanceof Player) {
               this.guillotineResetWindow.grant(var1.getGameTime());
               this.boss.resetGongdiComboCooldown();
               FTBossMod.LOGGER.info("[FTBoss][Gongdi] guillotine player kill: one recast available for 10 seconds, bleed={}", var3);
            }
         }
      }
   }

   private int stacks(LivingEntity var1, long var2) {
      GongdiSkills.BleedingTarget var4 = this.bleeding.get(var1.getUUID());
      return var4 != null && var4.player() == var1 ? var4.bleed().stacks(var2) : 0;
   }

   public boolean validTarget(Player player, double range) { return validTarget((LivingEntity) player, range); }

   public void onHit(Player player) { onHit((LivingEntity) player); }

   public void onAbilityHit(ServerLevel level, Player player, float damage) { onAbilityHit(level, (LivingEntity) player, damage); }

   private static record BleedingTarget(LivingEntity player, Bleed bleed) {
   }
}
