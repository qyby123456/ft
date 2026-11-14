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

   public SayaBuds(TeaTreeBossEntity boss) {
      this(boss, summon -> SummonSweep.belongsTo(boss, summon));
   }

   public SayaBuds(Mob boss, Predicate<Entity> owned) {
      this.boss = boss;
      this.owned = owned;
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
      if (this.boss.m_9236_() instanceof ServerLevel sl) {
         int var9 = 0;

         for (int i = 0; i < 5; i++) {
            TeaMinionEntity bud = (TeaMinionEntity)((EntityType)FTEntities.TEA_MINION.get()).m_20615_(sl);
            if (bud != null) {
               double angle = (Math.PI * 2.0 / 5.0) * (double)i + this.boss.m_217043_().m_188500_() * 0.4;
               double r = 4.5 + this.boss.m_217043_().m_188500_() * 2.5;
               bud.m_6034_(this.boss.m_20185_() + Math.cos(angle) * r, this.boss.m_20186_(), this.boss.m_20189_() + Math.sin(angle) * r);
               bud.makeGuardBud();
               bud.m_6593_(Component.m_237115_("entity.ftboss.guard_bud"));
               if (this.boss.m_5448_() != null) {
                  bud.m_6710_(this.boss.m_5448_());
               }

               ManorFightMembers.inherit(this.boss, bud);
               sl.m_7967_(bud);
               Fx.particles(sl, bud.m_20185_(), bud.m_20186_() + 0.7, bud.m_20189_(), 18, ParticleTypes.f_175833_, 0.35, 0.03);
               Fx.particles(sl, bud.m_20185_(), bud.m_20186_() + 0.7, bud.m_20189_(), 10, ParticleTypes.f_123748_, 0.3, 0.05);
               var9++;
            }
         }

         Fx.sound(sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11739_, 1.5F, 0.7F);
         Fx.sound(sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11767_, 1.0F, 1.1F);
         Fx.ring(sl, this.boss.m_20185_(), this.boss.m_20186_() + 0.2, this.boss.m_20189_(), 5.5, 40, ParticleTypes.f_123748_, 0.02);
         this.broadcast("msg.ftboss.saya_shield", var9);
         FTBossMod.LOGGER.info("[FTBoss] 沙耶 二阶段: 茶芽庇佑启动({} 株), 清空前完全免伤", var9);
      }
   }

   public void tick() {
      if (!this.boss.m_21224_()) {
         if (!this.shield) {
            if (this.boss.m_9236_() instanceof ServerLevel && this.aliveBuds() > 0) {
               this.shield = true;
               FTBossMod.LOGGER.info("[FTBoss] 沙耶 茶芽庇佑重新接管({} 株)", this.aliveBuds());
            }
         } else {
            int alive = this.aliveBuds();
            if (alive <= 0) {
               this.shield = false;
               if (this.boss.m_9236_() instanceof ServerLevel sl) {
                  Fx.sound(sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11738_, 1.5F, 0.8F);
                  Fx.sound(sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11983_, 1.4F, 0.7F);
                  Fx.particles(sl, this.boss.m_20185_(), this.boss.m_20186_() + 1.4, this.boss.m_20189_(), 45, ParticleTypes.f_123797_, 0.9, 0.12);
                  this.broadcast("msg.ftboss.saya_shield_break");
               }

               FTBossMod.LOGGER.info("[FTBoss] 沙耶 守护茶芽尽毁, 庇佑破除");
            } else {
               if (this.boss.f_19797_ % 5 == 0) {
                  Fx.dust(this.boss.m_9236_(), this.boss.m_20185_(), this.boss.m_20186_() + 1.1, this.boss.m_20189_(), 4, 0.2F, 0.75F, 0.45F, 1.0, 0.01);
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
                  this.remind(alive);
               }
            }
         }
      }
   }

   public void onBlocked(DamageSource source) {
      if (this.blockCd <= 0) {
         this.blockCd = 40;
         if (source.m_7639_() instanceof ServerPlayer p) {
            p.m_5661_(Component.m_237110_("msg.ftboss.saya_shield_hit", new Object[]{this.aliveBuds()}), true);
            Fx.sound(this.boss.m_9236_(), this.boss.m_20185_(), this.boss.m_20186_() + 1.0, this.boss.m_20189_(), SoundEvents.f_12346_, 1.0F, 1.3F);
         }
      }
   }

   public void reset() {
      this.shield = false;
      if (this.boss.m_9236_() instanceof ServerLevel) {
         for (TeaMinionEntity bud : this.collectBuds()) {
            bud.m_146870_();
         }
      }
   }

   private void channelBuffs() {
      if (this.boss.m_9236_() instanceof ServerLevel sl) {
         this.grant(new MobEffectInstance(MobEffects.f_19605_, 140, 1, false, false, true));
         this.grant(new MobEffectInstance(MobEffects.f_19606_, 140, 1, false, false, true));

         for (TeaMinionEntity bud : this.collectBuds()) {
            Fx.particles(sl, bud.m_20185_(), bud.m_20186_() + 0.7, bud.m_20189_(), 8, ParticleTypes.f_175833_, 0.25, 0.02);
            Fx.particles(sl, bud.m_20185_(), bud.m_20186_() + 1.0, bud.m_20189_(), 5, ParticleTypes.f_123748_, 0.25, 0.04);
         }

         Fx.sound(sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_144243_, 1.0F, 1.3F);
      }
   }

   private void grant(MobEffectInstance effect) {
      if (this.boss instanceof EasterBossEntity lemon) {
         lemon.lemonSacrifice().grant(effect);
      } else {
         this.boss.m_7292_(effect);
      }
   }

   private void remind(int alive) {
      if (this.boss.m_9236_() instanceof ServerLevel sl) {
         for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(48.0))) {
            p.m_5661_(Component.m_237110_("msg.ftboss.saya_shield_remind", new Object[]{alive}), true);
         }
      }
   }

   private void broadcast(String key, Object... args) {
      if (this.boss.m_9236_() instanceof ServerLevel sl) {
         for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(48.0))) {
            p.m_5661_(Component.m_237110_(key, args), false);
         }
      }
   }

   private List<TeaMinionEntity> collectBuds() {
      List<TeaMinionEntity> out = new ArrayList<>();
      if (this.boss.m_9236_().m_5776_()) {
         return out;
      } else {
         for (TeaMinionEntity m : this.boss.m_9236_().m_45976_(TeaMinionEntity.class, this.boss.m_20191_().m_82400_(64.0))) {
            if (m.m_6084_() && m.isGuardBud() && this.owned.test(m)) {
               out.add(m);
            }
         }

         return out;
      }
   }
}
