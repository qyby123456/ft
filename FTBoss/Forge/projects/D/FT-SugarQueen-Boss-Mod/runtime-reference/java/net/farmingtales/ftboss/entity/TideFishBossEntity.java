/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.ReefMinionEntity;
import net.farmingtales.ftboss.entity.TideBoltEntity;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.TideFishCombatRules;
import net.farmingtales.ftboss.util.WaterBlessing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TideFishBossEntity
extends Monster
implements GeoAnimatable,
RegularBossCastHost {
    public static final double MAX_HEALTH = 480.0;
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.m_135353_(TideFishBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<CompoundTag> ACTION = SynchedEntityData.m_135353_(TideFishBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final BossActionState action = new BossActionState((LivingEntity)this, ACTION);
    private static final EntityDataAccessor<CompoundTag> SIGNATURE_CAST_ACTION = SynchedEntityData.m_135353_(TideFishBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, SIGNATURE_CAST_ACTION);
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.tide_fish.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.tide_fish.signature_self_cast");
    private final ServerBossEvent bar;
    private final Set<UUID> hitTargets = new HashSet<UUID>();
    private Vec3 lockedDirection = Vec3.f_82478_;
    private Vec3 lockedCenter = Vec3.f_82478_;
    private String skill = "";
    private int skillTick;
    private int skillLength;
    private int cooldown = TideFishCombatRules.openingCooldown();
    private int rotation;
    private TideFishCombatRules.Skill activeSkill;
    private TideFishCombatRules.Skill pendingSkill;
    private Vec3[] hazardCenters = new Vec3[0];
    private Vec3 waveCenter = Vec3.f_82478_;
    private boolean dashBlocked;

    public TideFishBossEntity(EntityType<? extends TideFishBossEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 160;
        this.m_21530_();
        this.bar = new ServerBossEvent(this.m_5446_(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 480.0).m_22268_(Attributes.f_22281_, 10.0).m_22268_(Attributes.f_22284_, 4.0).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22277_, 40.0).m_22268_(Attributes.f_22283_, 5.6).m_22268_(Attributes.f_22278_, 0.85);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(PHASE, (Object)1);
        this.f_19804_.m_135372_(ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(SIGNATURE_CAST_ACTION, (Object)new CompoundTag());
    }

    public int getPhase() {
        return (Integer)this.f_19804_.m_135370_(PHASE);
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(RegularBossCastHost.action(kind), RegularBossCastHost.visualTicks(castTicks));
    }

    public boolean m_6040_() {
        return true;
    }

    public boolean m_7301_(MobEffectInstance effect) {
        return effect.m_19544_().m_19486_() && super.m_7301_(effect);
    }

    public boolean m_6785_(double distance) {
        return false;
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if (source.m_7639_() instanceof TideFishBossEntity || source.m_7639_() instanceof ReefMinionEntity) {
            return false;
        }
        float damage = BossDamageGate.gate((LivingEntity)this, source, amount);
        if (damage <= 0.0f) {
            return false;
        }
        boolean hit = super.m_6469_(source, damage);
        if (hit) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        return hit;
    }

    public void m_8119_() {
        LivingEntity target;
        ServerLevel server;
        block18: {
            block17: {
                super.m_8119_();
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block17;
                server = (ServerLevel)level;
                if (this.m_6084_()) break block18;
            }
            return;
        }
        BossBars.track((Entity)this, this.bar);
        BossDamageGate.tick((LivingEntity)this);
        BossRage.tickBoss((LivingEntity)this);
        TrapBreaker.tick((LivingEntity)this);
        WaterBlessing.tick((LivingEntity)this);
        this.bar.m_6456_(this.m_5446_());
        this.bar.m_142711_(Math.max(0.0f, Math.min(1.0f, this.m_21223_() / this.m_21233_())));
        if (this.f_19797_ % 10 == 0) {
            for (ServerPlayer viewer : Set.copyOf(this.bar.m_8324_())) {
                if (viewer.m_9236_() == this.m_9236_() && viewer.m_6084_() && !(this.m_20280_((Entity)viewer) > 2304.0)) continue;
                this.bar.m_6539_(viewer);
            }
            for (ServerPlayer viewer : server.m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
                if (!viewer.m_6084_() || !(this.m_20280_((Entity)viewer) <= 2304.0)) continue;
                BossBarVisibility.addPlayer(this.bar, viewer);
            }
        }
        if ((target = this.m_5448_()) == null || target.m_9236_() != this.m_9236_() || !target.m_6084_() || !BossTargets.valid(target) || this.m_20280_((Entity)target) > 1600.0) {
            target = BossTargets.nearest(server, (Entity)this, 40.0);
            this.m_6710_(target);
        }
        if (target != null) {
            BossMusic.request(server, (LivingEntity)this);
        } else {
            BossMusic.release(server, (LivingEntity)this);
        }
        if (this.getPhase() == 1 && this.m_21223_() <= this.m_21233_() * 0.5f) {
            this.f_19804_.m_135381_(PHASE, (Object)2);
            this.rotation = 0;
            this.activeSkill = null;
            this.pendingSkill = null;
            this.begin("phase", 40, target);
            this.bar.m_6451_(BossEvent.BossBarColor.PURPLE);
        }
        if (!this.skill.isEmpty()) {
            this.m_21573_().m_26573_();
            this.tickSkill(server);
            return;
        }
        if (target == null) {
            this.m_21573_().m_26573_();
            return;
        }
        this.m_21563_().m_24960_((Entity)target, 30.0f, 30.0f);
        if (this.m_20280_((Entity)target) > 10.0) {
            if (this.f_19797_ % 10 == 0) {
                this.m_21573_().m_5624_((Entity)target, this.getPhase() == 2 ? 1.5625 : 1.3125);
            }
        } else {
            this.m_21573_().m_26573_();
        }
        if (--this.cooldown > 0 || !this.m_142582_((Entity)target)) {
            return;
        }
        this.activeSkill = TideFishCombatRules.next(this.rotation);
        this.pendingSkill = TideFishCombatRules.next(this.rotation + 1);
        this.rotation = Math.floorMod(this.rotation + 2, TideFishCombatRules.Skill.values().length);
        this.begin(this.activeSkill.id, this.activeSkill.duration(this.getPhase() == 2), target);
    }

    private void begin(String name, int length, LivingEntity target) {
        this.skill = name;
        this.skillTick = 0;
        this.skillLength = length;
        this.hitTargets.clear();
        Vec3 vec3 = this.lockedDirection = target == null ? this.m_20154_() : target.m_20182_().m_82546_(this.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        if (this.lockedDirection.m_165925_() < 0.001) {
            this.lockedDirection = new Vec3(0.0, 0.0, 1.0);
        }
        this.lockedCenter = target == null ? this.m_20182_() : target.m_20182_();
        this.waveCenter = this.m_20182_();
        this.dashBlocked = false;
        int hazards = name.equals("geyser") ? TideFishCombatRules.geyserCount(this.getPhase() == 2) : (name.equals("maelstrom") ? TideFishCombatRules.maelstromCount(this.getPhase() == 2) : 0);
        this.hazardCenters = new Vec3[hazards];
        Vec3 sideways = new Vec3(this.lockedDirection.f_82481_, 0.0, -this.lockedDirection.f_82479_);
        for (int index = 0; index < hazards; ++index) {
            double offset = index == 0 ? 0.0 : (double)((index % 2 == 1 ? 1 : -1) * ((index + 1) / 2)) * 4.5;
            this.hazardCenters[index] = this.lockedCenter.m_82549_(sideways.m_82490_(offset));
        }
        this.faceLockedDirection();
        this.action.start(name + (this.getPhase() == 2 && !name.equals("phase") ? "_p2" : ""), length);
        this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
        this.m_21573_().m_26573_();
    }

    private void faceLockedDirection() {
        float facing = (float)Math.toDegrees(Math.atan2(-this.lockedDirection.f_82479_, this.lockedDirection.f_82481_));
        this.m_146922_(facing);
        this.f_20883_ = facing;
        this.f_20885_ = facing;
    }

    private void tickSkill(ServerLevel server) {
        int elapsed = ++this.skillTick;
        boolean enraged = this.getPhase() == 2;
        float damage = this.activeSkill == null ? 0.0f : this.activeSkill.damage(enraged);
        this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
        this.faceLockedDirection();
        switch (this.skill) {
            case "sweep": {
                if (!TideFishCombatRules.sweepAt(elapsed, enraged)) break;
                this.hitTargets.clear();
                this.strikeArea(server, this.m_20182_(), enraged ? 5.5 : 4.5, damage, elapsed == 4, true);
                this.sound(SoundEvents.f_12317_, elapsed == 4 ? 0.7f : 1.0f);
                break;
            }
            case "volley": {
                if (elapsed < 5 && elapsed % 2 == 0) {
                    server.m_8767_((ParticleOptions)ParticleTypes.f_123772_, this.m_20185_(), this.m_20188_(), this.m_20189_(), 8, 0.3, 0.3, 0.3, 0.02);
                }
                if (!TideFishCombatRules.volleyAt(elapsed, enraged)) break;
                Vec3 aim = this.lockedCenter.m_82520_(0.0, 1.0, 0.0).m_82546_(this.m_146892_()).m_82541_();
                int half = TideFishCombatRules.volleyBolts(enraged) / 2;
                for (int index = -half; index <= half; ++index) {
                    TideBoltEntity bolt = new TideBoltEntity((EntityType<? extends TideBoltEntity>)((EntityType)FTEntities.TIDE_BOLT.get()), (Level)server);
                    bolt.m_5602_((Entity)this);
                    bolt.setDamage(BossScaling.dmg((LivingEntity)this, damage));
                    bolt.m_6034_(this.m_20185_(), this.m_20188_() - 0.2, this.m_20189_());
                    Vec3 direction = aim.m_82524_((float)index * 0.14f);
                    bolt.m_6686_(direction.f_82479_, direction.f_82480_, direction.f_82481_, 1.3f, 0.0f);
                    server.m_7967_((Entity)bolt);
                }
                this.sound(SoundEvents.f_11821_, 0.8f);
                break;
            }
            case "dash": {
                if (enraged && elapsed == 19) {
                    this.lockedDirection = this.lockedDirection.m_82490_(-1.0);
                    this.faceLockedDirection();
                    this.hitTargets.clear();
                    this.dashBlocked = false;
                    this.sound(SoundEvents.f_12516_, 0.7f);
                }
                if (!TideFishCombatRules.dashActive(elapsed, enraged) || this.dashBlocked) break;
                if (elapsed == 5 || elapsed == 31) {
                    this.sound(SoundEvents.f_12517_, 0.85f);
                }
                Vec3 motion = this.lockedDirection.m_82490_(enraged ? 1.05 : 0.92);
                if (!server.m_45756_((Entity)this, this.m_20191_().m_82369_(motion))) {
                    this.dashBlocked = true;
                    this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
                    break;
                }
                this.m_20334_(motion.f_82479_, this.m_20184_().f_82480_, motion.f_82481_);
                this.strikeArea(server, this.m_20182_(), 2.6, damage, false, true);
                TideFishBossEntity.ring(server, this.m_20182_(), 1.4);
                break;
            }
            case "summon": {
                if (elapsed != 6) break;
                this.summon(server);
                break;
            }
            case "wave": {
                int age = TideFishCombatRules.waveAge(elapsed, enraged);
                if (age == 0) {
                    this.hitTargets.clear();
                    this.sound(SoundEvents.f_11917_, 0.7f);
                }
                if (age < 0) break;
                double radius = 1.0 + (double)age * 0.6;
                if (elapsed % 2 == 0) {
                    TideFishBossEntity.ring(server, this.waveCenter, radius);
                }
                for (ServerPlayer player : server.m_45976_(ServerPlayer.class, new AABB(this.waveCenter, this.waveCenter).m_82377_(12.0, 3.0, 12.0))) {
                    double distance = player.m_20182_().m_82546_(this.waveCenter).m_165924_();
                    if (!(Math.abs(distance - radius) < 0.8) || !(Math.abs(player.m_20186_() - this.waveCenter.f_82480_) < 0.75)) continue;
                    this.hit(player, damage, true);
                }
                break;
            }
            case "maelstrom": {
                for (int index = 0; index < this.hazardCenters.length; ++index) {
                    int release = 8 + index * 7;
                    Vec3 center = this.hazardCenters[index];
                    if (elapsed < release || elapsed >= release + 12 || elapsed % 3 != 0) continue;
                    TideFishBossEntity.ring(server, center.m_82520_(0.0, (double)(elapsed - release) * 0.16, 0.0), 3.5 - (double)(elapsed - release) * 0.2);
                }
                int pulse = TideFishCombatRules.maelstromAt(elapsed, enraged);
                if (pulse < 0) break;
                this.hitTargets.clear();
                this.strikeArea(server, this.hazardCenters[pulse], 3.5, damage, false, true);
                this.sound(SoundEvents.f_11776_, 0.7f);
                break;
            }
            case "geyser": {
                int pulse = TideFishCombatRules.geyserAt(elapsed, enraged);
                if (pulse < 0) break;
                Vec3 center = this.hazardCenters[pulse];
                this.hitTargets.clear();
                this.strikeArea(server, center, 1.8, damage, false, true);
                for (int height = 0; height < 6; ++height) {
                    server.m_8767_((ParticleOptions)ParticleTypes.f_123769_, center.f_82479_, center.f_82480_ + (double)height * 0.7, center.f_82481_, 14, 0.65, 0.25, 0.65, 0.12);
                }
                this.sound(SoundEvents.f_11775_, enraged ? 0.8f : 1.0f);
                break;
            }
            case "phase": {
                if (elapsed % 5 == 0) {
                    TideFishBossEntity.ring(server, this.m_20182_().m_82520_(0.0, 1.2, 0.0), 3.0);
                }
                if (elapsed != 20) break;
                this.sound(SoundEvents.f_11767_, 0.75f);
                break;
            }
        }
        if (this.skillTick >= this.skillLength) {
            this.skill = "";
            this.activeSkill = null;
            this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
            if (this.pendingSkill != null) {
                TideFishCombatRules.Skill queued = this.pendingSkill;
                this.pendingSkill = null;
                this.activeSkill = queued;
                this.begin(queued.id, queued.duration(enraged), this.m_5448_());
                this.cooldown = 0;
            } else {
                this.cooldown = TideFishCombatRules.cooldown(enraged);
            }
        }
    }

    private void strikeArea(ServerLevel server, Vec3 center, double radius, float damage, boolean cone, boolean once) {
        for (ServerPlayer player : server.m_45976_(ServerPlayer.class, new AABB(center, center).m_82377_(radius, 2.5, radius))) {
            Vec3 offset = player.m_20182_().m_82546_(center);
            if (offset.m_165925_() > radius * radius || cone && offset.m_82542_(1.0, 0.0, 1.0).m_82541_().m_82526_(this.lockedDirection) < -0.1) continue;
            this.hit(player, damage, once);
        }
        if (!once) {
            this.sound(SoundEvents.f_11917_, 0.65f);
        }
    }

    private void hit(ServerPlayer target, float damage, boolean once) {
        if (!BossTargets.valid((Player)target) || !this.m_142582_((Entity)target)) {
            return;
        }
        if (once && !this.hitTargets.add(target.m_20148_())) {
            return;
        }
        if (target.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), BossScaling.dmg((LivingEntity)this, damage))) {
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0));
            Vec3 push = target.m_20182_().m_82546_(this.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_().m_82490_(0.45);
            BossKnockback.push((Entity)target, push.f_82479_, 0.15, push.f_82481_);
            target.f_19864_ = true;
        }
    }

    private void summon(ServerLevel server) {
        int owned = server.m_6443_(ReefMinionEntity.class, this.m_20191_().m_82400_(64.0), minion -> this.m_20148_().equals(minion.ownerId())).size();
        int count = TideFishCombatRules.summonCount(this.getPhase() == 2, owned);
        for (int index = 0; index < count; ++index) {
            long nearby;
            ReefMinionEntity minion2 = (ReefMinionEntity)((EntityType)(index % 2 == 0 ? FTEntities.REEF_FISH : FTEntities.BRINE_PUFFER).get()).m_20615_((Level)server);
            if (minion2 == null || (nearby = (long)server.m_6443_(ReefMinionEntity.class, this.m_20191_().m_82400_(64.0), existing -> existing.m_6084_() && existing.m_6095_() == minion2.m_6095_()).size()) >= 10L) continue;
            double angle = (double)index * Math.PI * 2.0 / (double)Math.max(1, count);
            minion2.m_7678_(this.m_20185_() + Math.cos(angle) * 3.0, this.m_20186_() + 0.3, this.m_20189_() + Math.sin(angle) * 3.0, this.m_146908_(), 0.0f);
            minion2.setOwnerId(this.m_20148_());
            minion2.m_6710_(this.m_5448_());
            if (this.getPhase() == 2) {
                minion2.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 600, 0));
                minion2.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 200, 0));
            }
            if (!server.m_45786_((Entity)minion2)) continue;
            server.m_7967_((Entity)minion2);
        }
        this.sound(SoundEvents.f_12291_, 0.7f);
    }

    private static void ring(ServerLevel server, Vec3 center, double radius) {
        for (int index = 0; index < 40; ++index) {
            double angle = (double)index * Math.PI * 2.0 / 40.0;
            server.m_8767_((ParticleOptions)ParticleTypes.f_123769_, center.f_82479_ + Math.cos(angle) * radius, center.f_82480_ + 0.2, center.f_82481_ + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void sound(SoundEvent sound, float pitch) {
        this.m_9236_().m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), sound, SoundSource.HOSTILE, 1.2f, pitch);
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        this.cleanup();
    }

    public void m_142687_(Entity.RemovalReason reason) {
        this.cleanup();
        super.m_142687_(reason);
    }

    private void cleanup() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        BossBars.untrack((Entity)this, this.bar);
        BossMusic.release(server, (LivingEntity)this);
        for (ReefMinionEntity minion : server.m_45976_(ReefMinionEntity.class, this.m_20191_().m_82400_(96.0))) {
            if (!this.m_20148_().equals(minion.ownerId())) continue;
            minion.m_146870_();
        }
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_("TidePhase", this.getPhase());
        tag.m_128405_("TideRotation", this.rotation);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.f_19804_.m_135381_(PHASE, (Object)(tag.m_128451_("TidePhase") == 2 ? 2 : 1));
        this.rotation = Math.max(0, tag.m_128451_("TideRotation"));
        this.bar.m_6451_(this.getPhase() == 2 ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.BLUE);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "tide", 3, state -> {
            this.action.prepare(state);
            this.signatureCast.prepare(state);
            if (this.m_6084_() && this.signatureCast.active()) {
                return state.setAndContinue("signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST);
            }
            String name = !this.m_6084_() ? "death" : (this.action.active() ? this.action.name() : (state.isMoving() ? "walk" : "idle"));
            return state.setAndContinue(name.equals("idle") || name.equals("walk") ? RawAnimation.begin().thenLoop("animation.tide_fish." + name) : RawAnimation.begin().thenPlay("animation.tide_fish." + name));
        })});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object object) {
        return this.f_19797_;
    }
}

