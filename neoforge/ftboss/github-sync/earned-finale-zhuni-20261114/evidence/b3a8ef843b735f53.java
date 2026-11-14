package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayDeque;
import java.util.Comparator;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ai.XuanlanSkyStrike.PendingStrike;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanSkyRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class XuanlanSkyStrike {
   private final EasterBossEntity boss;
   private int uses;
   private int age;
   private int strikes;
   private int warnings;
   private int recovery;
   private boolean active;
   private boolean oldInvisible;
   private boolean oldNoGravity;
   private boolean oldNoPhysics;
   private Vec3 home = Vec3.ZERO;
   private Vec3 landing = Vec3.ZERO;
   private final ArrayDeque<PendingStrike> pendingStrikes = new ArrayDeque<>();
   private Player target;

   public XuanlanSkyStrike(EasterBossEntity var1) {
      this.boss = var1;
   }

   public boolean active() {
      return this.active;
   }

   public boolean tick(ServerLevel var1) {
      if (!this.active) {
         if (this.recovery > 0) {
            this.recovery--;
            return false;
         }

         if (this.boss.getVariant() != 6
            || this.boss.isMini()
            || this.boss.isGiant()
            || this.boss.isNoAi()
            || this.boss.isInvulnerable()
            || this.boss.isPerformingSkill()
            || ManorFight.isFinaleMember(this.boss)
            || HiddenBossIntro.isActing(this.boss.getUUID())
            || !XuanlanSkyRules.ready(this.uses, this.boss.getHealth(), this.boss.getMaxHealth())
            || !(this.boss.getTarget() instanceof Player var2)
            || !this.valid(var2, this.boss.position())) {
            return false;
         }

         Vec3 var8 = ManorSafeTeleport.find(var1, this.boss, this.boss.position(), 4);
         if (var8 == null) {
            return false;
         }

         this.home = var8;
         this.landing = this.home;
         this.target = var2;
         this.oldInvisible = this.boss.isInvisible();
         this.oldNoGravity = this.boss.isNoGravity();
         this.oldNoPhysics = this.boss.noPhysics;
         this.active = true;
         this.uses++;
         this.age = 0;
         this.strikes = 0;
         this.warnings = 0;
         this.pendingStrikes.clear();
         this.boss.stopRiding();
         this.boss.setNoGravity(true);
         this.boss.noPhysics = true;
         this.send(var1, 20, this.home, this.home.add(0.0, 24.0, 0.0));
         var1.playSound(null, this.home.x, this.home.y, this.home.z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.8F, 1.5F);

         for (ServerPlayer var5 : var1.players()) {
            if (var5.distanceToSqr(this.home) <= 4096.0) {
               var5.displayClientMessage(Component.literal("§b§l玄蓝 · 天穹锁击 §f60连击！§e离开脚下预警圈！"), true);
            }
         }

         FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} begin use={}/{}", new Object[]{this.boss.getUUID(), this.uses, 2});
      }

      if (this.boss.isAlive() && !this.boss.isNoAi() && !ManorFight.isFinaleMember(this.boss)) {
         this.boss.getNavigation().stop();
         this.boss.setDeltaMovement(Vec3.ZERO);
         this.boss.fallDistance = 0.0F;
         if (this.age < 20) {
            this.move(this.home.add(0.0, 24.0 * XuanlanSkyRules.rise(((double)this.age + 1.0) / 20.0), 0.0));
            var1.sendParticles(ParticleTypes.END_ROD, this.boss.getX(), this.boss.getY() + 0.8, this.boss.getZ(), 5, 0.35, 0.5, 0.35, 0.04);
         } else {
            int var7 = this.age - 20;
            int var9 = XuanlanSkyRules.attackTicks();
            if (var7 < var9) {
               this.boss.setXuanlanSkyHidden(true);
               this.boss.setInvisible(true);
               this.move(this.home.add(0.0, 24.0, 0.0));
               if (this.warnings < 60 && var7 == XuanlanSkyRules.warningTick(this.warnings)) {
                  if (!this.valid(this.target, this.home)) {
                     this.target = var1.players()
                        .stream()
                        .filter(var1x -> this.valid(var1x, this.home))
                        .min(Comparator.comparingDouble(var1x -> var1x.distanceToSqr(this.home)))
                        .orElse(null);
                  }

                  if (this.target == null) {
                     this.cancel();
                     return true;
                  }

                  Vec3 var10 = this.target.position();
                  Vec3 var12 = var1.clip(new ClipContext(var10.add(0.0, 0.5, 0.0), var10.add(0.0, -12.0, 0.0), Block.COLLIDER, Fluid.NONE, this.boss))
                     .getLocation()
                     .add(0.0, 0.04, 0.0);
                  Vec3 var6 = var12.add(0.0, Math.max(24.0, var10.y - var12.y + 8.0), 0.0);
                  this.pendingStrikes.addLast(new PendingStrike(var12, var6, var7 + 10));
                  this.warnings++;
                  this.send(var1, 21, var12, var6);
                  var1.playSound(null, var12.x, var12.y, var12.z, (SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.HOSTILE, 1.2F, 1.6F);
               }

               while (!this.pendingStrikes.isEmpty() && this.pendingStrikes.peekFirst().impactAge() <= var7) {
                  this.strike(var1, this.pendingStrikes.removeFirst());
                  this.strikes++;
               }
            } else {
               int var11 = var7 - var9;
               if (var11 == 0) {
                  Vec3 var13 = ManorSafeTeleport.find(var1, this.boss, this.home, 8);
                  if (var13 != null) {
                     this.landing = var13;
                  }

                  this.boss.setXuanlanSkyHidden(false);
                  this.boss.setInvisible(this.oldInvisible);
                  this.send(var1, 23, this.landing, this.landing.add(0.0, 24.0, 0.0));
               }

               this.move(this.landing.add(0.0, 24.0 * (1.0 - XuanlanSkyRules.rise(((double)var11 + 1.0) / 20.0)), 0.0));
               if (var11 + 1 >= 20) {
                  this.cancel();
                  return true;
               }
            }
         }

         this.age++;
         return true;
      } else {
         this.cancel();
         return true;
      }
   }

   private boolean valid(Player var1, Vec3 var2) {
      return var1 != null
         && var1.level() == this.boss.level()
         && var1.isAlive()
         && !var1.isRemoved()
         && !var1.isCreative()
         && !var1.isSpectator()
         && !this.boss.isAlliedTo(var1)
         && var1.distanceToSqr(var2) <= 4096.0;
   }

   private void move(Vec3 var1) {
      this.boss.teleportTo(var1.x, var1.y, var1.z);
      this.boss.setDeltaMovement(Vec3.ZERO);
      this.boss.fallDistance = 0.0F;
   }

   private void strike(ServerLevel var1, PendingStrike var2) {
      Vec3 var3 = var2.locked();
      Vec3 var4 = var2.beamTop();
      this.send(var1, 22, var3, var4);
      var1.playSound(null, var3.x, var3.y, var3.z, SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.5F, 1.4F);
      var1.sendParticles(ParticleTypes.END_ROD, var3.x, var3.y + 0.3, var3.z, 24, 1.4, 0.3, 1.4, 0.16);
      double var5 = 3.0;
      AABB var7 = new AABB(var3.x - var5, var3.y - 0.1, var3.z - var5, var3.x + var5, var4.y, var3.z + var5);

      for (Player var9 : var1.getEntitiesOfClass(Player.class, var7, var1x -> this.valid(var1x, this.home))) {
         AABB var10 = var9.getBoundingBox();
         double var11 = Math.max(var10.minX, Math.min(var3.x, var10.maxX));
         double var13 = Math.max(var10.minZ, Math.min(var3.z, var10.maxZ));
         if (!((var11 - var3.x) * (var11 - var3.x) + (var13 - var3.z) * (var13 - var3.z) > var5 * var5)) {
            int var15 = var9.invulnerableTime;
            var9.invulnerableTime = 0;

            try {
               var9.hurt(var1.damageSources().indirectMagic(this.boss, this.boss), this.boss.manorSkillDamage(var9, 230.0F, 2.0));
            } finally {
               var9.invulnerableTime = Math.max(var15, var9.invulnerableTime);
            }
         }
      }
   }

   private void send(ServerLevel var1, int var2, Vec3 var3, Vec3 var4) {
      XuanlanFxPacket.send(var1, var2, this.boss.getId(), var3, var4);
   }

   public void cancel() {
      if (this.active) {
         if (this.boss.level() instanceof ServerLevel var1) {
            this.send(var1, 24, this.home, this.boss.position());
            Vec3 var3 = ManorSafeTeleport.find(var1, this.boss, this.landing, 8);
            this.move(var3 == null ? this.home : var3);
            var1.playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.2F, 1.4F);
         }

         this.boss.setXuanlanSkyHidden(false);
         this.boss.setInvisible(this.oldInvisible);
         this.boss.setNoGravity(this.oldNoGravity);
         this.boss.noPhysics = this.oldNoPhysics;
         this.active = false;
         this.pendingStrikes.clear();
         this.target = null;
         this.recovery = 200;
         FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} end strikes={} uses={}", new Object[]{this.boss.getUUID(), this.strikes, this.uses});
      }
   }

   public void save(CompoundTag var1) {
      CompoundTag var2 = new CompoundTag();
      var2.putInt("Uses", this.uses);
      var2.putInt("Recovery", this.recovery);
      var2.putBoolean("Active", this.active);
      var2.putBoolean("Invisible", this.oldInvisible);
      var2.putBoolean("NoGravity", this.oldNoGravity);
      var2.putBoolean("NoPhysics", this.oldNoPhysics);
      var2.putDouble("HomeX", this.home.x);
      var2.putDouble("HomeY", this.home.y);
      var2.putDouble("HomeZ", this.home.z);
      var1.put("XuanlanSkyStrike", var2);
   }

   public void load(CompoundTag var1) {
      CompoundTag var2 = var1.getCompound("XuanlanSkyStrike");
      this.uses = Math.max(0, Math.min(2, var2.getInt("Uses")));
      this.recovery = Math.max(0, Math.min(200, var2.getInt("Recovery")));
      if (var2.getBoolean("Active")) {
         this.home = new Vec3(var2.getDouble("HomeX"), var2.getDouble("HomeY"), var2.getDouble("HomeZ"));
         if (!Double.isFinite(this.home.x) || !Double.isFinite(this.home.y) || !Double.isFinite(this.home.z)) {
            this.home = this.boss.position();
         }

         this.landing = this.home;
         this.oldInvisible = var2.getBoolean("Invisible");
         this.oldNoGravity = var2.getBoolean("NoGravity");
         this.oldNoPhysics = var2.getBoolean("NoPhysics");
         this.active = true;
         this.cancel();
      }
   }
}
