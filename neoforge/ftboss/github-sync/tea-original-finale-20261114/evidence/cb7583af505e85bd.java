package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class SayaBuds {
   private static final int BUD_COUNT = 5;
   private static final double SCAN = 64.0;
   private static final double PLAYER_RANGE = 48.0;
   private static final int BUFF_CD = 40;
   private static final int REMIND_CD = 100;
   private static final int BLOCK_NOTIFY_CD = 40;
   private final Mob boss;
   private final Predicate<Entity> owned;
   private boolean shield = false;
   private int buffCd = 40;
   private int remindCd = 20;
   private int blockCd = 0;

   public SayaBuds(TeaTreeBossEntity var1) {
      this(var1, var1x -> SummonSweep.belongsTo(var1, var1x));
   }

   public SayaBuds(Mob var1, Predicate<Entity> var2) {
      this.boss = var1;
      this.owned = var2;
   }

   public boolean shielded() {
      return this.shield;
   }

   public int aliveBuds() {
      return this.collectBuds().size();
   }

   public void trigger() {
      this.shield = true;
      this.buffCd = 30;
      this.remindCd = 20;
      this.blockCd = 0;
      if (this.boss.level() instanceof ServerLevel var1) {
         int var9 = 0;

         for (int var3 = 0; var3 < 5; var3++) {
            TeaMinionEntity var4 = (TeaMinionEntity)((EntityType)FTEntities.TEA_MINION.get()).create(var1);
            if (var4 != null) {
               double var5 = (Math.PI * 2.0 / 5.0) * (double)var3 + this.boss.getRandom().nextDouble() * 0.4;
               double var7 = 4.5 + this.boss.getRandom().nextDouble() * 2.5;
               var4.setPos(this.boss.getX() + Math.cos(var5) * var7, this.boss.getY(), this.boss.getZ() + Math.sin(var5) * var7);
               var4.makeGuardBud();
               var4.setCustomName(Component.translatable("entity.ftboss.guard_bud"));
               if (this.boss.getTarget() != null) {
                  var4.setTarget(this.boss.getTarget());
               }

               ManorFightMembers.inherit(this.boss, var4);
               var1.addFreshEntity(var4);
               Fx.particles(var1, var4.getX(), var4.getY() + 0.7, var4.getZ(), 18, ParticleTypes.SPORE_BLOSSOM_AIR, 0.35, 0.03);
               Fx.particles(var1, var4.getX(), var4.getY() + 0.7, var4.getZ(), 10, ParticleTypes.HAPPY_VILLAGER, 0.3, 0.05);
               var9++;
            }
         }

         Fx.sound(var1, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_POWER_SELECT, 1.5F, 0.7F);
         Fx.sound(var1, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.CONDUIT_ACTIVATE, 1.0F, 1.1F);
         Fx.ring(var1, this.boss.getX(), this.boss.getY() + 0.2, this.boss.getZ(), 5.5, 40, ParticleTypes.HAPPY_VILLAGER, 0.02);
         this.broadcast("msg.ftboss.saya_shield", var9);
         FTBossMod.LOGGER.info("[FTBoss] 沙耶 二阶段: 茶芽庇佑启动({} 株), 清空前完全免伤", var9);
      }
   }

   public void tick() {
      if (!this.boss.isDeadOrDying()) {
         if (!this.shield) {
            if (this.boss.level() instanceof ServerLevel && this.aliveBuds() > 0) {
               this.shield = true;
               FTBossMod.LOGGER.info("[FTBoss] 沙耶 茶芽庇佑重新接管({} 株)", this.aliveBuds());
            }
         } else {
            int var1 = this.aliveBuds();
            if (var1 <= 0) {
               this.shield = false;
               if (this.boss.level() instanceof ServerLevel var2) {
                  Fx.sound(var2, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_DEACTIVATE, 1.5F, 0.8F);
                  Fx.sound(var2, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.GLASS_BREAK, 1.4F, 0.7F);
                  Fx.particles(var2, this.boss.getX(), this.boss.getY() + 1.4, this.boss.getZ(), 45, ParticleTypes.CRIT, 0.9, 0.12);
                  this.broadcast("msg.ftboss.saya_shield_break");
               }

               FTBossMod.LOGGER.info("[FTBoss] 沙耶 守护茶芽尽毁, 庇佑破除");
            } else {
               if (this.boss.tickCount % 5 == 0) {
                  Fx.dust(this.boss.level(), this.boss.getX(), this.boss.getY() + 1.1, this.boss.getZ(), 4, 0.2F, 0.75F, 0.45F, 1.0, 0.01);
               }

               if (--this.buffCd <= 0) {
                  this.buffCd = 40;
                  this.channelBuffs();
               }

               if (this.blockCd > 0) {
                  this.blockCd--;
               }

               if (--this.remindCd <= 0) {
                  this.remindCd = 100;
                  this.remind(var1);
               }
            }
         }
      }
   }

   public void onBlocked(DamageSource var1) {
      if (this.blockCd <= 0) {
         this.blockCd = 40;
         if (var1.getEntity() instanceof ServerPlayer var2) {
            var2.displayClientMessage(Component.translatable("msg.ftboss.saya_shield_hit", new Object[]{this.aliveBuds()}), true);
            Fx.sound(this.boss.level(), this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(), SoundEvents.SHIELD_BLOCK, 1.0F, 1.3F);
         }
      }
   }

   public void reset() {
      this.shield = false;
      if (this.boss.level() instanceof ServerLevel) {
         for (TeaMinionEntity var2 : this.collectBuds()) {
            var2.discard();
         }
      }
   }

   private void channelBuffs() {
      if (this.boss.level() instanceof ServerLevel var1) {
         this.grant(new MobEffectInstance(MobEffects.REGENERATION, 140, 1, false, false, true));
         this.grant(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 1, false, false, true));

         for (TeaMinionEntity var3 : this.collectBuds()) {
            Fx.particles(var1, var3.getX(), var3.getY() + 0.7, var3.getZ(), 8, ParticleTypes.SPORE_BLOSSOM_AIR, 0.25, 0.02);
            Fx.particles(var1, var3.getX(), var3.getY() + 1.0, var3.getZ(), 5, ParticleTypes.HAPPY_VILLAGER, 0.25, 0.04);
         }

         Fx.sound(var1, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.3F);
      }
   }

   private void grant(MobEffectInstance var1) {
      if (this.boss instanceof EasterBossEntity var2) {
         var2.lemonSacrifice().grant(var1);
      } else {
         this.boss.addEffect(var1);
      }
   }

   private void remind(int var1) {
      if (this.boss.level() instanceof ServerLevel var2) {
         for (ServerPlayer var4 : var2.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(48.0))) {
            var4.displayClientMessage(Component.translatable("msg.ftboss.saya_shield_remind", new Object[]{var1}), true);
         }
      }
   }

   private void broadcast(String var1, Object... var2) {
      if (this.boss.level() instanceof ServerLevel var3) {
         for (ServerPlayer var5 : var3.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(48.0))) {
            var5.displayClientMessage(Component.translatable(var1, var2), false);
         }
      }
   }

   private List<TeaMinionEntity> collectBuds() {
      ArrayList var1 = new ArrayList();
      if (this.boss.level().isClientSide()) {
         return var1;
      } else {
         for (TeaMinionEntity var3 : this.boss.level().getEntitiesOfClass(TeaMinionEntity.class, this.boss.getBoundingBox().inflate(64.0))) {
            if (var3.isAlive() && var3.isGuardBud() && this.owned.test(var3)) {
               var1.add(var3);
            }
         }

         return var1;
      }
   }
}
