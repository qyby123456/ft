/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.turret;

import java.util.Comparator;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionHooks;
import net.farmingtales.ftboss.balance.ManorExpansionRules;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.turret.TurretFlywheelEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.farmingtales.ftboss.turret.ZhuniTurretDrops;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class XuanlanTurretEntity
extends Mob {
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.m_135353_(XuanlanTurretEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> FIRED = SynchedEntityData.m_135353_(XuanlanTurretEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private UUID owner;
    private int age;

    public XuanlanTurretEntity(EntityType<? extends XuanlanTurretEntity> entityType, Level level) {
        super(entityType, level);
        this.m_21557_(true);
        this.m_21153_(150.0f);
        this.m_6593_((Component)Component.m_237113_((String)"\u7384\u84dd\u6708\u8f6e\u70ae\u53f0"));
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 150.0).m_22268_(Attributes.f_22284_, 50.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 1.0);
    }

    public int m_21230_() {
        return 50;
    }

    public UUID ownerId() {
        return this.owner;
    }

    public void setOwner(EasterBossEntity easterBossEntity) {
        this.owner = easterBossEntity.m_20148_();
    }

    public int age() {
        return this.m_9236_().f_46443_ ? (Integer)this.f_19804_.m_135370_(AGE) : this.age;
    }

    public float recoil(float f) {
        return Math.max(0.0f, 1.0f - ((float)this.age() + f - (float)((Integer)this.f_19804_.m_135370_(FIRED)).intValue()) / 5.0f);
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(AGE, (Object)0);
        this.f_19804_.m_135372_(FIRED, (Object)-100);
    }

    public void m_8119_() {
        Vec3 vec3;
        Entity entity;
        super.m_8119_();
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        if (!(this.owner != null && (entity = serverLevel.m_8791_(this.owner)) instanceof EasterBossEntity && (object = (EasterBossEntity)entity).m_6084_() && !object.m_213877_() && ((EasterBossEntity)object).getVariant() == 6 && ManorBalanceHooks.mainBoss((EasterBossEntity)object) && ZhuniTurretDrops.inEncounter((EasterBossEntity)object, (Entity)this) && ++this.age < 600)) {
            this.m_146870_();
            return;
        }
        this.f_19804_.m_135381_(AGE, (Object)this.age);
        if (!this.m_6084_() || object.m_21525_() || object.m_20147_() || object.m_20145_() || ((EasterBossEntity)object).isManorIntroHidden() || !ManorExpansionRules.turretFires(this.age)) {
            return;
        }
        entity = this.m_20182_().m_82520_(0.0, 1.35, 0.0);
        ServerPlayer serverPlayer = ManorBalanceHooks.players((EasterBossEntity)object, serverLevel).stream().filter(arg_0 -> this.lambda$tick$0(serverLevel, (Vec3)entity, arg_0)).min(Comparator.comparingDouble(arg_0 -> ((XuanlanTurretEntity)this).m_20280_(arg_0))).orElse(null);
        if (serverPlayer == null) {
            return;
        }
        Vec3 vec32 = serverPlayer.m_20191_().m_82399_();
        Vec3 vec33 = vec32.m_82546_((Vec3)entity);
        this.m_146922_((float)Math.toDegrees(Math.atan2(-vec33.f_82479_, vec33.f_82481_)));
        this.f_19804_.m_135381_(FIRED, (Object)this.age);
        Vec3 vec34 = vec33.m_82541_();
        for (ServerPlayer object2 : ManorBalanceHooks.players((EasterBossEntity)object, serverLevel)) {
            vec3 = object2.m_20191_().m_82399_().m_82546_((Vec3)entity);
            double d = vec3.m_82526_(vec34);
            if (d < 0.0 || d > vec33.m_82553_() + 0.01 || vec3.m_82546_(vec34.m_82490_(d)).m_82553_() > 2.6 + (double)object2.m_20205_() * 0.5 || !ManorExpansionHooks.clear(serverLevel, (Vec3)entity, object2.m_20191_().m_82399_(), (Entity)this)) continue;
            float f = ((EasterBossEntity)object).manorSkillDamage((LivingEntity)object2, 251.4f, 2.0) / 3.0f;
            object2.f_19802_ = 0;
            object2.m_6469_(serverLevel.m_269111_().m_269333_((LivingEntity)object), f);
        }
        for (double d = 0.0; d <= vec33.m_82553_(); d += 0.25) {
            vec3 = entity.m_82549_(vec33.m_82541_().m_82490_(d));
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123745_, vec32.f_82479_, vec32.f_82480_, vec32.f_82481_, 8, 0.2, 0.2, 0.2, 0.03);
        serverLevel.m_5594_(null, this.m_20183_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 0.7f, 1.5f);
        for (int i = 0; i < 3; ++i) {
            TurretFlywheelEntity turretFlywheelEntity = new TurretFlywheelEntity(XuanlanTurrets.FLYWHEEL, (Level)serverLevel);
            turretFlywheelEntity.launch((EasterBossEntity)object, serverPlayer, (Vec3)entity, i);
            ManorFightMembers.inherit((Entity)object, turretFlywheelEntity);
            serverLevel.m_7967_((Entity)turretFlywheelEntity);
        }
    }

    public boolean m_6469_(DamageSource damageSource, float f) {
        block3: {
            block2: {
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block2;
                ServerLevel serverLevel = (ServerLevel)level;
                if (!Float.isFinite(f) || f <= 0.0f || this.owner == null || !((level = serverLevel.m_8791_(this.owner)) instanceof EasterBossEntity)) break block2;
                EasterBossEntity easterBossEntity = (EasterBossEntity)level;
                level = damageSource.m_7639_();
                if (!(level instanceof ServerPlayer)) break block2;
                ServerPlayer serverPlayer = (ServerPlayer)level;
                if (ManorBalanceHooks.players(easterBossEntity, serverLevel).contains(serverPlayer)) break block3;
            }
            return false;
        }
        return super.m_6469_(damageSource, f);
    }

    public void m_7380_(CompoundTag compoundTag) {
        super.m_7380_(compoundTag);
        if (this.owner != null) {
            compoundTag.m_128362_("Owner", this.owner);
        }
        compoundTag.m_128405_("Age", this.age);
    }

    public void m_7378_(CompoundTag compoundTag) {
        super.m_7378_(compoundTag);
        this.owner = compoundTag.m_128403_("Owner") ? compoundTag.m_128342_("Owner") : null;
        this.age = Math.max(0, compoundTag.m_128451_("Age"));
    }

    private /* synthetic */ boolean lambda$tick$0(ServerLevel serverLevel, Vec3 vec3, ServerPlayer serverPlayer) {
        return this.m_20280_((Entity)serverPlayer) <= 1024.0 && ManorExpansionHooks.clear(serverLevel, vec3, serverPlayer.m_20191_().m_82399_(), (Entity)this);
    }
}

