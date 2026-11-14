package net.farmingtales.ftboss.turret;

import java.util.Comparator;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionHooks;
import net.farmingtales.ftboss.balance.ManorExpansionRules;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class XuanlanTurretEntity extends Mob {
   private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.m_135353_(XuanlanTurretEntity.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Integer> FIRED = SynchedEntityData.m_135353_(XuanlanTurretEntity.class, EntityDataSerializers.f_135028_);
   private UUID owner;
   private int age;

   public XuanlanTurretEntity(EntityType<? extends XuanlanTurretEntity> var1, Level var2) {
      super(var1, var2);
      this.m_21557_(true);
      this.m_21153_(150.0F);
      this.m_6593_(Component.m_237113_("玄蓝月轮炮台"));
   }

   public static Builder attributes() {
      return Mob.m_21552_()
         .m_22268_(Attributes.f_22276_, 150.0)
         .m_22268_(Attributes.f_22284_, 50.0)
         .m_22268_(Attributes.f_22279_, 0.0)
         .m_22268_(Attributes.f_22278_, 1.0);
   }

   public int m_21230_() {
      return 50;
   }

   public UUID ownerId() {
      return this.owner;
   }

   public void setOwner(EasterBossEntity var1) {
      this.owner = var1.m_20148_();
   }

   public int age() {
      return this.m_9236_().f_46443_ ? (Integer)this.f_19804_.m_135370_(AGE) : this.age;
   }

   public float recoil(float var1) {
      return Math.max(0.0F, 1.0F - ((float)this.age() + var1 - (float)((Integer)this.f_19804_.m_135370_(FIRED)).intValue()) / 5.0F);
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(AGE, 0);
      this.f_19804_.m_135372_(FIRED, -100);
   }

   public void m_8119_() {
      super.m_8119_();
      if (this.m_9236_() instanceof ServerLevel var1) {
         if (this.owner != null
            && var1.m_8791_(this.owner) instanceof EasterBossEntity var14
            && var14.m_6084_()
            && !var14.m_213877_()
            && var14.getVariant() == 6
            && ManorBalanceHooks.mainBoss(var14)
            && ZhuniTurretDrops.inEncounter(var14, this)
            && ++this.age < 600) {
            this.f_19804_.m_135381_(AGE, this.age);
            if (this.m_6084_()
               && !var14.m_21525_()
               && !var14.m_20147_()
               && !var14.m_20145_()
               && !var14.isManorIntroHidden()
               && ManorExpansionRules.turretFires(this.age)) {
               Vec3 var15 = this.m_20182_().m_82520_(0.0, 1.35, 0.0);
               ServerPlayer var4 = ManorBalanceHooks.players(var14, var1)
                  .stream()
                  .filter(var3 -> this.m_20280_(var3) <= 1024.0 && ManorExpansionHooks.clear(var1, var15, var3.m_20191_().m_82399_(), this))
                  .min(Comparator.comparingDouble(this::m_20280_))
                  .orElse(null);
               if (var4 != null) {
                  Vec3 var5 = var4.m_20191_().m_82399_();
                  Vec3 var6 = var5.m_82546_(var15);
                  this.m_146922_((float)Math.toDegrees(Math.atan2(-var6.f_82479_, var6.f_82481_)));
                  this.f_19804_.m_135381_(FIRED, this.age);
                  Vec3 var7 = var6.m_82541_();

                  for (ServerPlayer var9 : ManorBalanceHooks.players(var14, var1)) {
                     Vec3 var10 = var9.m_20191_().m_82399_().m_82546_(var15);
                     double var11 = var10.m_82526_(var7);
                     if (!(var11 < 0.0)
                        && !(var11 > var6.m_82553_() + 0.01)
                        && !(var10.m_82546_(var7.m_82490_(var11)).m_82553_() > 2.6 + (double)var9.m_20205_() * 0.5)
                        && ManorExpansionHooks.clear(var1, var15, var9.m_20191_().m_82399_(), this)) {
                        float var13 = var14.manorSkillDamage(var9, 251.4F, 2.0) / 3.0F;
                        var9.f_19802_ = 0;
                        var9.m_6469_(var1.m_269111_().m_269333_(var14), var13);
                     }
                  }

                  for (double var16 = 0.0; var16 <= var6.m_82553_(); var16 += 0.25) {
                     Vec3 var19 = var15.m_82549_(var6.m_82541_().m_82490_(var16));
                     var1.m_8767_(ParticleTypes.f_123810_, var19.f_82479_, var19.f_82480_, var19.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
                     var1.m_8767_(ParticleTypes.f_175830_, var19.f_82479_, var19.f_82480_, var19.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
                  }

                  var1.m_8767_(ParticleTypes.f_123745_, var5.f_82479_, var5.f_82480_, var5.f_82481_, 8, 0.2, 0.2, 0.2, 0.03);
                  var1.m_5594_(null, this.m_20183_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 0.7F, 1.5F);

                  for (int var17 = 0; var17 < 3; var17++) {
                     TurretFlywheelEntity var18 = new TurretFlywheelEntity(XuanlanTurrets.FLYWHEEL, var1);
                     var18.launch(var14, var4, var15, var17);
                     ManorFightMembers.inherit(var14, var18);
                     var1.m_7967_(var18);
                  }
               }
            }
         } else {
            this.m_146870_();
         }
      }
   }

   public boolean m_6469_(DamageSource var1, float var2) {
      if (this.m_9236_() instanceof ServerLevel var3
         && Float.isFinite(var2)
         && !(var2 <= 0.0F)
         && this.owner != null
         && var3.m_8791_(this.owner) instanceof EasterBossEntity var4
         && var1.m_7639_() instanceof ServerPlayer var5
         && ManorBalanceHooks.players(var4, var3).contains(var5)) {
         return super.m_6469_(var1, var2);
      }

      return false;
   }

   public void m_7380_(CompoundTag var1) {
      super.m_7380_(var1);
      if (this.owner != null) {
         var1.m_128362_("Owner", this.owner);
      }

      var1.m_128405_("Age", this.age);
   }

   public void m_7378_(CompoundTag var1) {
      super.m_7378_(var1);
      this.owner = var1.m_128403_("Owner") ? var1.m_128342_("Owner") : null;
      this.age = Math.max(0, var1.m_128451_("Age"));
   }
}
