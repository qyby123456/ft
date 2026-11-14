/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.damage.DamageSources
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells;
import net.farmingtales.ftboss.integ.spells.QizaiSpell;
import net.farmingtales.ftboss.util.QizaiArsenalRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class QizaiStarEntity
extends Projectile {
    private static final EntityDataAccessor<Boolean> DATA_TRACKING = SynchedEntityData.m_135353_(QizaiStarEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private UUID trackingTarget;
    private int lifetime = 60;
    private boolean spellCast;
    private float spellDamage;

    public QizaiStarEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public QizaiStarEntity(ServerLevel serverLevel, EasterBossEntity easterBossEntity, Vec3 vec3) {
        this((EntityType)FTEntities.QIZAI_STAR.get(), (Level)serverLevel);
        this.m_5602_((Entity)easterBossEntity);
        this.m_146884_(vec3);
        ManorFightMembers.inherit((Entity)easterBossEntity, (Entity)this);
    }

    public QizaiStarEntity(ServerLevel serverLevel, LivingEntity livingEntity, Vec3 vec3, float f) {
        this((EntityType)FTEntities.QIZAI_STAR.get(), (Level)serverLevel);
        this.m_5602_((Entity)livingEntity);
        this.m_146884_(vec3);
        this.spellCast = true;
        this.spellDamage = Float.isFinite(f) ? Math.max(0.0f, Math.min(100000.0f, f)) : 0.0f;
        ManorFightMembers.inherit((Entity)livingEntity, (Entity)this);
    }

    protected void m_8097_() {
        this.f_19804_.m_135372_(DATA_TRACKING, (Object)false);
    }

    public boolean isTrackingMissile() {
        return (Boolean)this.f_19804_.m_135370_(DATA_TRACKING);
    }

    public void track(LivingEntity livingEntity, int n) {
        this.trackingTarget = livingEntity.m_20148_();
        this.lifetime = Math.max(1, Math.min(60, n));
        this.f_19804_.m_135381_(DATA_TRACKING, (Object)true);
    }

    private void steer(ServerLevel serverLevel) {
        if (this.trackingTarget == null || this.spellCast) {
            return;
        }
        LivingEntity livingEntity = ManorLivingTargets.resolve((Level)serverLevel, this.trackingTarget);
        if (livingEntity == null || !livingEntity.m_6084_() || ManorLivingTargets.creative(livingEntity) || livingEntity.m_5833_() || livingEntity.m_213877_() || livingEntity.m_20280_((Entity)this) > 2304.0 || this.m_19749_() == null || this.m_19749_().m_7307_((Entity)livingEntity) || livingEntity.m_7307_(this.m_19749_())) {
            this.trackingTarget = null;
            return;
        }
        Vec3 vec3 = livingEntity.m_20191_().m_82399_().m_82546_(this.m_20182_()).m_82541_();
        Vec3 vec32 = this.m_20184_().m_82541_();
        double d = Math.max(-1.0, Math.min(1.0, vec32.m_82526_(vec3)));
        double d2 = Math.acos(d);
        double d3 = QizaiArsenalRules.MISSILE_TURN;
        Vec3 vec33 = vec3;
        if (d2 > d3) {
            Vec3 vec34 = vec3.m_82546_(vec32.m_82490_(d)).m_82541_();
            if (vec34.m_82556_() < 0.01) {
                vec34 = vec32.m_82537_(Math.abs(vec32.f_82480_) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).m_82541_();
            }
            vec33 = vec32.m_82490_(Math.cos(d3)).m_82549_(vec34.m_82490_(Math.sin(d3))).m_82541_();
        }
        this.m_20256_(vec33.m_82490_(0.58));
    }

    public void m_8119_() {
        super.m_8119_();
        if (!this.m_9236_().f_46443_) {
            Mob mob;
            LivingEntity livingEntity;
            Entity entity = this.m_19749_();
            if (!(entity instanceof LivingEntity) || !(livingEntity = (LivingEntity)entity).m_6084_() || livingEntity.m_213877_() || livingEntity.m_5833_() || livingEntity instanceof Mob && (mob = (Mob)livingEntity).m_21525_() || livingEntity.m_9236_() != this.m_9236_() || !this.spellCast && !(livingEntity instanceof EasterBossEntity) || --this.lifetime <= 0) {
                this.m_146870_();
                return;
            }
            entity = this.m_9236_();
            if (entity instanceof ServerLevel) {
                mob = (ServerLevel)entity;
                this.steer((ServerLevel)mob);
            }
            if ((mob = ProjectileUtil.m_278158_((Entity)this, this::m_5603_)).m_6662_() != HitResult.Type.MISS) {
                if (mob instanceof EntityHitResult) {
                    entity = (EntityHitResult)mob;
                    if (this.spellCast) {
                        QizaiSpell qizaiSpell = QizaiPlayerSpells.spell(QizaiPlayerSpells.Kind.STAR);
                        DamageSources.applyDamage((Entity)entity.m_82443_(), (float)this.spellDamage, (DamageSource)qizaiSpell.getDamageSource((Entity)this, (Entity)livingEntity));
                    } else {
                        entity.m_82443_().m_6469_(this.m_9236_().m_269111_().m_269104_((Entity)this, (Entity)livingEntity), 48.0f);
                    }
                }
                this.m_146870_();
                return;
            }
        }
        this.m_146884_(this.m_20182_().m_82549_(this.m_20184_()));
        this.m_37283_();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected boolean m_5603_(Entity entity) {
        if (!ManorLivingTargets.canHit((Entity)this, entity)) {
            return false;
        }
        if (this.isTrackingMissile() && this.m_19749_() != null) {
            if (this.m_19749_().m_7307_(entity)) return false;
            if (entity.m_7307_(this.m_19749_())) {
                return false;
            }
        }
        if (this.spellCast) {
            Entity entity2 = this.m_19749_();
            if (!(entity2 instanceof LivingEntity)) return false;
            LivingEntity livingEntity = (LivingEntity)entity2;
            if (!(entity instanceof LivingEntity)) return false;
            LivingEntity livingEntity2 = (LivingEntity)entity;
            if (!BossSpellEffects.canTarget(livingEntity, livingEntity2)) return false;
            if (!super.m_5603_(entity)) return false;
            return true;
        }
        if (!(entity instanceof LivingEntity)) return false;
        LivingEntity livingEntity = (LivingEntity)entity;
        if (!livingEntity.m_6084_()) return false;
        if (ManorLivingTargets.creative(livingEntity)) return false;
        if (livingEntity.m_5833_()) return false;
        if (!super.m_5603_(entity)) return false;
        return true;
    }

    protected void m_7380_(CompoundTag compoundTag) {
        super.m_7380_(compoundTag);
        compoundTag.m_128405_("Life", this.lifetime);
        if (this.trackingTarget != null) {
            compoundTag.m_128362_("TrackingTarget", this.trackingTarget);
        }
        compoundTag.m_128379_("Tracking", this.isTrackingMissile());
        compoundTag.m_128379_("QizaiSpell", this.spellCast);
        compoundTag.m_128350_("SpellDamage", this.spellDamage);
    }

    protected void m_7378_(CompoundTag compoundTag) {
        super.m_7378_(compoundTag);
        this.lifetime = Math.max(1, Math.min(60, compoundTag.m_128451_("Life")));
        this.trackingTarget = compoundTag.m_128403_("TrackingTarget") ? compoundTag.m_128342_("TrackingTarget") : null;
        this.f_19804_.m_135381_(DATA_TRACKING, (Object)compoundTag.m_128471_("Tracking"));
        this.spellCast = compoundTag.m_128471_("QizaiSpell");
        this.spellDamage = Float.isFinite(compoundTag.m_128457_("SpellDamage")) ? Math.max(0.0f, Math.min(100000.0f, compoundTag.m_128457_("SpellDamage"))) : 0.0f;
    }

    public Packet m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

