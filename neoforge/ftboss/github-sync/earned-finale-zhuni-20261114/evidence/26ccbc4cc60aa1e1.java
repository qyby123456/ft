package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.body.BodySignals;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
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
   private Vec3 home = Vec3.f_82478_;
   private Vec3 landing = Vec3.f_82478_;
   private final ArrayDeque<PendingStrike> pendingStrikes = new ArrayDeque<>();
   private LivingEntity target;

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

         LivingEntity var2;
         if (this.boss.getVariant() != 6
            || this.boss.isMini()
            || this.boss.isGiant()
            || this.boss.m_21525_()
            || this.boss.m_20147_()
            || this.boss.isPerformingSkill()
            || ManorFight.isFinaleMember(this.boss)
            || HiddenBossIntro.isActing(this.boss.m_20148_())
            || !XuanlanSkyRules.ready(this.uses, this.boss.m_21223_(), this.boss.m_21233_())
            || !((var2 = this.boss.m_5448_()) instanceof LivingEntity)
            || !this.valid(var2, this.boss.m_20182_())) {
            return false;
         }

         Vec3 var3 = ManorSafeTeleport.find(var1, this.boss, this.boss.m_20182_(), 4);
         if (var3 == null) {
            return false;
         }

         this.landing = this.home = var3;
         this.target = var2;
         this.oldInvisible = this.boss.m_20145_();
         this.oldNoGravity = this.boss.m_20068_();
         this.oldNoPhysics = this.boss.f_19794_;
         this.active = true;
         this.uses++;
         this.age = 0;
         this.strikes = 0;
         this.warnings = 0;
         this.pendingStrikes.clear();
         this.boss.m_8127_();
         this.boss.m_20242_(true);
         this.boss.f_19794_ = true;
         this.send(var1, 20, this.home, this.home.m_82520_(0.0, 24.0, 0.0));
         var1.m_6263_(null, this.home.f_82479_, this.home.f_82480_, this.home.f_82481_, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.8F, 1.5F);

         for (LivingEntity var5 : ManorLivingTargets.nearby(var1, this.boss)) {
            if (var5.m_20238_(this.home) <= 4096.0) {
               ManorLivingTargets.tell(var5, Component.m_237113_("§b§l玄蓝 · 天穹锁击 §f60连击！§e离开脚下预警圈！"), true);
            }
         }

         FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} begin use={}/{}", new Object[]{this.boss.m_20148_(), this.uses, 2});
      }

      if (this.boss.m_6084_() && !this.boss.m_21525_() && !ManorFight.isFinaleMember(this.boss)) {
         this.boss.m_21573_().m_26573_();
         this.boss.m_20256_(Vec3.f_82478_);
         this.boss.f_19789_ = 0.0F;
         if (this.age < 20) {
            this.move(this.home.m_82520_(0.0, 24.0 * XuanlanSkyRules.rise(((double)this.age + 1.0) / 20.0), 0.0));
            var1.m_8767_(ParticleTypes.f_123810_, this.boss.m_20185_(), this.boss.m_20186_() + 0.8, this.boss.m_20189_(), 5, 0.35, 0.5, 0.35, 0.04);
         } else {
            int var10 = this.age - 20;
            int var11 = XuanlanSkyRules.attackTicks();
            if (var10 < var11) {
               this.boss.setXuanlanSkyHidden(true);
               this.boss.m_6842_(true);
               this.move(this.home.m_82520_(0.0, 24.0, 0.0));
               if (this.warnings < 60 && var10 == XuanlanSkyRules.warningTick(this.warnings)) {
                  ArrayList var12 = new ArrayList();

                  for (LivingEntity var6 : ManorLivingTargets.nearby(var1, this.boss)) {
                     if (this.valid(var6, this.home)) {
                        var12.add(var6);
                     }
                  }

                  if (var12.isEmpty() && this.valid(this.target, this.home)) {
                     var12.add(this.target);
                  }

                  if (var12.isEmpty()) {
                     this.cancel();
                     return true;
                  }

                  this.target = (LivingEntity)var12.get(0);
                  Vec3 var15 = null;

                  for (LivingEntity var7 : var12) {
                     Vec3 var8 = var7.m_20182_();
                     var8 = var1.m_45547_(new ClipContext(var8.m_82520_(0.0, 0.5, 0.0), var8.m_82520_(0.0, -12.0, 0.0), Block.COLLIDER, Fluid.NONE, this.boss))
                        .m_82450_()
                        .m_82520_(0.0, 0.04, 0.0);
                     Vec3 var9 = var8.m_82520_(0.0, Math.max(24.0, var8.f_82480_ - var8.f_82480_ + 8.0), 0.0);
                     this.pendingStrikes.addLast(new PendingStrike(var8, var9, var10 + 10));
                     this.queuePrediction(var1, var7, var10 + 10);
                     this.send(var1, 21, var8, var9);
                     if (var15 == null) {
                        var15 = var8;
                     }
                  }

                  this.warnings++;
                  if (var15 != null) {
                     var1.m_6263_(
                        null, var15.f_82479_, var15.f_82480_, var15.f_82481_, (SoundEvent)SoundEvents.f_12211_.m_203334_(), SoundSource.HOSTILE, 1.2F, 1.6F
                     );
                  }
               }

               while (!this.pendingStrikes.isEmpty() && this.pendingStrikes.peekFirst().impactAge() <= var10) {
                  this.strike(var1, this.pendingStrikes.removeFirst());
                  this.strikes++;
               }
            } else {
               int var13 = var10 - var11;
               if (var13 == 0) {
                  Vec3 var16 = ManorSafeTeleport.find(var1, this.boss, this.home, 8);
                  if (var16 != null) {
                     this.landing = var16;
                  }

                  this.boss.setXuanlanSkyHidden(false);
                  this.boss.m_6842_(this.oldInvisible);
                  this.send(var1, 23, this.landing, this.landing.m_82520_(0.0, 24.0, 0.0));
               }

               this.move(this.landing.m_82520_(0.0, 24.0 * (1.0 - XuanlanSkyRules.rise(((double)var13 + 1.0) / 20.0)), 0.0));
               if (var13 + 1 >= 20) {
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

   private boolean valid(LivingEntity var1, Vec3 var2) {
      return !ManorLivingTargets.valid(this.boss, var1)
         ? false
         : var1 != null
            && var1.m_9236_() == this.boss.m_9236_()
            && var1.m_6084_()
            && !var1.m_213877_()
            && !ManorLivingTargets.creative(var1)
            && !var1.m_5833_()
            && !this.boss.m_7307_(var1)
            && var1.m_20238_(var2) <= 4096.0;
   }

   private void move(Vec3 var1) {
      this.boss.m_6021_(var1.f_82479_, var1.f_82480_, var1.f_82481_);
      this.boss.m_20256_(Vec3.f_82478_);
      this.boss.f_19789_ = 0.0F;
   }

   private void strike(ServerLevel var1, PendingStrike var2) {
      Vec3 var3 = var2.locked();
      Vec3 var4 = var2.beamTop();
      this.send(var1, 22, var3, var4);
      var1.m_6263_(null, var3.f_82479_, var3.f_82480_, var3.f_82481_, SoundEvents.f_12001_, SoundSource.HOSTILE, 1.5F, 1.4F);
      var1.m_8767_(ParticleTypes.f_123810_, var3.f_82479_, var3.f_82480_ + 0.3, var3.f_82481_, 24, 1.4, 0.3, 1.4, 0.16);
      double var5 = 3.0;
      AABB var7 = new AABB(var3.f_82479_ - var5, var3.f_82480_ - 0.1, var3.f_82481_ - var5, var3.f_82479_ + var5, var4.f_82480_, var3.f_82481_ + var5);

      for (LivingEntity var9 : var1.m_6443_(LivingEntity.class, var7, var1x -> this.valid(var1x, this.home))) {
         AABB var12 = var9.m_20191_();
         double var13 = Math.max(var12.f_82288_, Math.min(var3.f_82479_, var12.f_82291_));
         double var10;
         if (!(
            (var13 - var3.f_82479_) * (var13 - var3.f_82479_)
                  + ((var10 = Math.max(var12.f_82290_, Math.min(var3.f_82481_, var12.f_82293_))) - var3.f_82481_) * (var10 - var3.f_82481_)
               > var5 * var5
         )) {
            int var15 = var9.f_19802_;
            var9.f_19802_ = 0;

            try {
               var9.m_6469_(var1.m_269111_().m_269104_(this.boss, this.boss), this.boss.manorSkillDamage(var9, 230.0F, 2.0));
            } finally {
               var9.f_19802_ = Math.max(var15, var9.f_19802_);
            }
         }
      }
   }

   private void send(ServerLevel var1, int var2, Vec3 var3, Vec3 var4) {
      BodySignals.sky(this.boss, var2);
      XuanlanFxPacket.send(var1, var2, this.boss.m_19879_(), var3, var4);
   }

   public void cancel() {
      if (this.active) {
         if (this.boss.m_9236_() instanceof ServerLevel var2) {
            this.send(var2, 24, this.home, this.boss.m_20182_());
            Vec3 var3 = ManorSafeTeleport.find(var2, this.boss, this.landing, 8);
            this.move(var3 == null ? this.home : var3);
            var2.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11738_, SoundSource.HOSTILE, 1.2F, 1.4F);
         }

         this.boss.setXuanlanSkyHidden(false);
         this.boss.m_6842_(this.oldInvisible);
         this.boss.m_20242_(this.oldNoGravity);
         this.boss.f_19794_ = this.oldNoPhysics;
         this.active = false;
         this.pendingStrikes.clear();
         this.target = null;
         this.recovery = 200;
         FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} end strikes={} uses={}", new Object[]{this.boss.m_20148_(), this.strikes, this.uses});
      }
   }

   public void save(CompoundTag var1) {
      CompoundTag var2 = new CompoundTag();
      var2.m_128405_("Uses", this.uses);
      var2.m_128405_("Recovery", this.recovery);
      var2.m_128379_("Active", this.active);
      var2.m_128379_("Invisible", this.oldInvisible);
      var2.m_128379_("NoGravity", this.oldNoGravity);
      var2.m_128379_("NoPhysics", this.oldNoPhysics);
      var2.m_128347_("HomeX", this.home.f_82479_);
      var2.m_128347_("HomeY", this.home.f_82480_);
      var2.m_128347_("HomeZ", this.home.f_82481_);
      var1.m_128365_("XuanlanSkyStrike", var2);
   }

   public void load(CompoundTag var1) {
      CompoundTag var2 = var1.m_128469_("XuanlanSkyStrike");
      this.uses = Math.max(0, Math.min(2, var2.m_128451_("Uses")));
      this.recovery = Math.max(0, Math.min(200, var2.m_128451_("Recovery")));
      if (var2.m_128471_("Active")) {
         this.home = new Vec3(var2.m_128459_("HomeX"), var2.m_128459_("HomeY"), var2.m_128459_("HomeZ"));
         if (!Double.isFinite(this.home.f_82479_) || !Double.isFinite(this.home.f_82480_) || !Double.isFinite(this.home.f_82481_)) {
            this.home = this.boss.m_20182_();
         }

         this.landing = this.home;
         this.oldInvisible = var2.m_128471_("Invisible");
         this.oldNoGravity = var2.m_128471_("NoGravity");
         this.oldNoPhysics = var2.m_128471_("NoPhysics");
         this.active = true;
         this.cancel();
      }
   }

   private void queuePrediction(ServerLevel var1, LivingEntity var2, int var3) {
      Vec3 var4 = ManorPredictiveAttacks.skyPrediction(var1, this.boss, var2, this.home);
      if (var4 != null) {
         Vec3 var5 = var4.m_82520_(0.0, Math.max(24.0, var2.m_20186_() - var4.f_82480_ + 8.0), 0.0);
         this.pendingStrikes.addLast(new PendingStrike(var4, var5, var3));
         XuanlanFxPacket.send(var1, 21, this.boss.m_19879_(), var4, var5);
      }
   }
}
