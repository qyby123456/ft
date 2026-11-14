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
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.TraceableEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.QizaiCrystalMissileEntity;
import net.farmingtales.ftboss.integ.QizaiCrystalBridge;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaicross.TemporaryTriads;
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.skill.qizaiparty.MultiplayerCrystals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalEntity
extends Mob
implements TraceableEntity {
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.m_135353_(QizaiCrystalEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<CompoundTag> CUE = SynchedEntityData.m_135353_(QizaiCrystalEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private static final EntityDataAccessor<Float> WARD = SynchedEntityData.m_135353_(QizaiCrystalEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135029_);
    private UUID ownerId;
    private int cooldown = 30;
    private int phase;
    private int phaseTicks;
    private int wardTicks;
    private Vec3 aim = Vec3.f_82478_;
    private Entity shield;
    private final List<QizaiCrystalMissileEntity> missiles = new ArrayList<QizaiCrystalMissileEntity>();

    public QizaiCrystalEntity(EntityType<? extends QizaiCrystalEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
        this.f_21364_ = 0;
    }

    public QizaiCrystalEntity(ServerLevel level, EasterBossEntity boss, int role) {
        this((EntityType<? extends QizaiCrystalEntity>)((EntityType)FTEntities.QIZAI_CRYSTAL.get()), (Level)level);
        this.ownerId = boss.m_20148_();
        this.f_19804_.m_135381_(ROLE, (Object)role);
        this.m_6593_((Component)Component.m_237115_((String)("entity.ftboss.qizai_crystal." + role)));
        this.cooldown += role * 10;
        ManorFightMembers.inherit((Entity)boss, (Entity)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 120.0).m_22268_(Attributes.f_22281_, 0.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 1.0).m_22268_(Attributes.f_22277_, 36.0);
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(ROLE, (Object)0);
        this.f_19804_.m_135372_(CUE, (Object)new CompoundTag());
        this.f_19804_.m_135372_(WARD, (Object)Float.valueOf(0.0f));
    }

    public EasterBossEntity getOwner() {
        EasterBossEntity boss;
        ServerLevel level;
        Level level2;
        return this.ownerId != null && (level2 = this.m_9236_()) instanceof ServerLevel && (level2 = (level = (ServerLevel)level2).m_8791_(this.ownerId)) instanceof EasterBossEntity ? (boss = (EasterBossEntity)level2) : null;
    }

    public int role() {
        return Math.max(0, Math.min(2, (Integer)this.f_19804_.m_135370_(ROLE)));
    }

    public CompoundTag cue() {
        return (CompoundTag)this.f_19804_.m_135370_(CUE);
    }

    public float ward() {
        return ((Float)this.f_19804_.m_135370_(WARD)).floatValue();
    }

    public Entity shield() {
        return this.shield;
    }

    public void m_7023_(Vec3 input) {
        if (!this.m_9236_().f_46443_) {
            this.m_6478_(MoverType.SELF, this.m_20184_());
        }
    }

    public boolean m_142535_(float distance, float multiplier, DamageSource source) {
        return false;
    }

    public boolean m_6785_(double distance) {
        return false;
    }

    public boolean m_6573_(Player player) {
        return false;
    }

    protected void m_6668_(DamageSource source) {
    }

    public boolean m_6469_(DamageSource source, float amount) {
        QizaiCrystalEntity crystal;
        Entity entity;
        if (source.m_7639_() == this.getOwner() || (entity = source.m_7639_()) instanceof QizaiCrystalEntity && (crystal = (QizaiCrystalEntity)entity).getOwner() == this.getOwner()) {
            return false;
        }
        return super.m_6469_(source, amount);
    }

    public void m_8119_() {
        Vec3 direction;
        super.m_8119_();
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        EasterBossEntity owner = this.getOwner();
        if (!this.m_6084_() || this.m_21525_() || !QizaiCrystals.validOwner(owner) || !QizaiCrystals.current(this)) {
            this.m_146870_();
            return;
        }
        this.missiles.removeIf(Entity::m_213877_);
        LivingEntity primary = QizaiCrystals.combatTarget(owner);
        LivingEntity target = primary == null ? null : this.selectTarget(level2, owner, primary);
        this.orbit(owner, primary);
        if (this.wardTicks > 0 && --this.wardTicks == 0) {
            this.clearWard();
        }
        if (this.shield != null && !this.shield.m_213877_() && primary != null) {
            direction = primary.m_20182_().m_82546_(owner.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            QizaiCrystalBridge.move(this.shield, owner.m_20182_().m_82549_(direction.m_82490_(2.8)).m_82520_(0.0, 1.4, 0.0), QizaiCrystalEntity.yaw(direction));
        }
        if (target == null) {
            this.phase = 0;
            this.f_19804_.m_135381_(CUE, (Object)new CompoundTag());
            this.clearWard();
            return;
        }
        if (this.role() == 0 && this.phase != 0) {
            this.laser(level2, owner, target);
            return;
        }
        this.cooldown = CrossGuard.arm(this.cooldown, this) - 1;
        if (this.cooldown > 0) {
            return;
        }
        if (this.role() == 0) {
            this.phase = 1;
            this.phaseTicks = 16;
            this.aim = target.m_146892_();
            this.syncBeam(level2);
            level2.m_5594_(null, this.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 0.9f, 1.5f);
        } else if (this.role() == 1) {
            for (int index = 0; index < 9; ++index) {
                QizaiCrystalMissileEntity missile = new QizaiCrystalMissileEntity(level2, this, owner, target);
                missile.m_146884_(this.m_146892_());
                float spread = ((float)index / 8.0f - 0.5f) * 0.9f;
                missile.m_20256_(target.m_146892_().m_82546_(this.m_146892_()).m_82541_().m_82524_(spread).m_82490_(0.65));
                if (!level2.m_7967_((Entity)missile)) continue;
                this.missiles.add(missile);
            }
            level2.m_5594_(null, this.m_20183_(), SoundEvents.f_11862_, SoundSource.HOSTILE, 0.7f, 1.6f);
            this.cooldown = 100;
        } else {
            this.clearWard();
            this.wardTicks = 120;
            this.f_19804_.m_135381_(WARD, (Object)Float.valueOf(240.0f));
            direction = target.m_20182_().m_82546_(owner.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            this.shield = QizaiCrystalBridge.create(level2, owner, owner.m_20182_().m_82549_(direction.m_82490_(2.8)).m_82520_(0.0, 1.4, 0.0), QizaiCrystalEntity.yaw(direction));
            level2.m_5594_(null, this.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 0.8f, 1.4f);
            this.cooldown = 200;
        }
    }

    private LivingEntity selectTarget(ServerLevel level, EasterBossEntity owner, LivingEntity primary) {
        if (this.role() == 2) {
            return primary;
        }
        if (this.m_20280_((Entity)primary) <= 1296.0 && this.m_142582_((Entity)primary)) {
            return primary;
        }
        return level.m_6907_().stream().filter(player -> QizaiCrystals.target(owner, (Entity)player) && this.m_20280_((Entity)player) <= 1296.0 && this.m_142582_((Entity)player)).min(Comparator.comparingDouble(arg_0 -> ((QizaiCrystalEntity)this).m_20280_(arg_0))).orElse(null);
    }

    private void orbit(EasterBossEntity owner, LivingEntity target) {
        double angle = CrossGuard.angle(MultiplayerCrystals.angle(TemporaryTriads.angle((double)this.f_19797_ * 0.022 + (double)this.role() * Math.PI * 2.0 / 3.0, this), this), this);
        Vec3 preferred = owner.m_20182_().m_82520_(Math.cos(angle) * CrossGuard.radius(MultiplayerCrystals.radius(TemporaryTriads.radius(4.2, this), this), this), CrossGuard.height(MultiplayerCrystals.height(TemporaryTriads.height(3.2, this), this), this) + Math.sin((double)this.f_19797_ * 0.06 + (double)this.role()) * CrossGuard.bob(MultiplayerCrystals.bob(TemporaryTriads.bob(0.4, this), this), this), Math.sin(angle) * CrossGuard.radius(MultiplayerCrystals.radius(TemporaryTriads.radius(4.2, this), this), this));
        Vec3 movement = preferred.m_82546_(this.m_20182_());
        if (this.m_20280_((Entity)owner) > 2304.0) {
            this.m_146870_();
            return;
        }
        this.m_20256_(movement.m_82541_().m_82490_(Math.min(0.32, movement.m_82553_() * 0.14)));
        if (target != null) {
            this.m_21563_().m_24960_((Entity)target, 90.0f, 90.0f);
        }
    }

    private static float yaw(Vec3 direction) {
        return (float)Math.toDegrees(Math.atan2(-direction.f_82479_, direction.f_82481_));
    }

    private Vec3 clippedAim() {
        Vec3 from = this.m_146892_();
        Vec3 direction = this.aim.m_82546_(from);
        Vec3 end = from.m_82549_(direction.m_82541_().m_82490_(Math.min(36.0, direction.m_82553_() + 1.0)));
        return this.m_9236_().m_45547_(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this)).m_82450_();
    }

    private void laser(ServerLevel serverLevel, EasterBossEntity easterBossEntity, LivingEntity livingEntity2) {
        if (this.phase == 1 && this.phaseTicks > 4) {
            this.aim = livingEntity2.m_146892_();
        }
        if (this.phase == 1 && --this.phaseTicks == 0) {
            this.phase = 2;
            this.phaseTicks = 9;
            serverLevel.m_5594_(null, this.m_20183_(), SoundEvents.f_12001_, SoundSource.HOSTILE, 0.8f, 1.6f);
        }
        if (this.phase == 2) {
            if ((9 - this.phaseTicks) % 4 == 0) {
                Vec3 vec3 = this.m_146892_();
                Vec3 vec32 = this.clippedAim();
                List list = serverLevel.m_6443_(LivingEntity.class, new AABB(vec3, vec32).m_82400_(0.3), livingEntity -> QizaiCrystals.target(easterBossEntity, (Entity)livingEntity));
                list.stream().filter(livingEntity -> livingEntity.m_20191_().m_82400_(0.25).m_82371_(vec3, vec32).isPresent()).min(Comparator.comparingDouble(arg_0 -> ((QizaiCrystalEntity)this).m_20280_(arg_0))).ifPresent(livingEntity -> {
                    livingEntity.f_19802_ = 0;
                    livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this, (Entity)easterBossEntity), 32.0f);
                });
            }
            if (--this.phaseTicks <= 0) {
                this.phase = 0;
                this.cooldown = 80;
                this.f_19804_.m_135381_(CUE, (Object)new CompoundTag());
                return;
            }
        }
        this.syncBeam(serverLevel);
    }

    private void syncBeam(ServerLevel level) {
        CompoundTag cue = new CompoundTag();
        Vec3 end = this.clippedAim();
        cue.m_128405_("phase", this.phase);
        cue.m_128356_("until", level.m_46467_() + (long)this.phaseTicks + 2L);
        cue.m_128347_("x", end.f_82479_);
        cue.m_128347_("y", end.f_82480_);
        cue.m_128347_("z", end.f_82481_);
        this.f_19804_.m_135381_(CUE, (Object)cue);
    }

    public float absorb(float requested) {
        if (!this.m_6084_() || this.m_21525_() || this.wardTicks <= 0 || !Float.isFinite(requested) || requested <= 0.0f) {
            return 0.0f;
        }
        float absorbed = Math.min(requested, this.ward());
        this.f_19804_.m_135381_(WARD, (Object)Float.valueOf(this.ward() - absorbed));
        return absorbed;
    }

    private void clearWard() {
        this.f_19804_.m_135381_(WARD, (Object)Float.valueOf(0.0f));
        this.wardTicks = 0;
        if (this.shield != null) {
            this.shield.m_146870_();
            this.shield = null;
        }
    }

    public void m_142687_(Entity.RemovalReason reason) {
        this.clearWard();
        this.missiles.forEach(Entity::m_146870_);
        this.missiles.clear();
        super.m_142687_(reason);
    }

    public void m_6667_(DamageSource source) {
        this.clearWard();
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.m_8767_((ParticleOptions)ParticleTypes.f_123810_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 18, 0.4, 0.6, 0.4, 0.08);
        }
        super.m_6667_(source);
        this.m_146870_();
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.ownerId != null) {
            tag.m_128362_("CrystalOwner", this.ownerId);
        }
        tag.m_128405_("CrystalRole", this.role());
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.ownerId = tag.m_128403_("CrystalOwner") ? tag.m_128342_("CrystalOwner") : null;
        this.f_19804_.m_135381_(ROLE, (Object)Math.max(0, Math.min(2, tag.m_128451_("CrystalRole"))));
    }
}

