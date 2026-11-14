/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.PoopCurse;
import net.farmingtales.ftboss.boss.ai.SummonLimit;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.entity.CompostSlimeEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.FlyMinionEntity;
import net.farmingtales.ftboss.entity.MaggotMinionEntity;
import net.farmingtales.ftboss.entity.PoopMinionEntity;
import net.farmingtales.ftboss.entity.RainbowPoopEntity;
import net.farmingtales.ftboss.entity.SheepDungEntity;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossCollision;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CompostBossEntity
extends Monster
implements GeoAnimatable,
RegularBossCastHost {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(CompostBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(CompostBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, DATA_SIGNATURE_CAST);
    private static final float P2_AT = 0.7f;
    private static final float P3_AT = 0.35f;
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.m_135353_(CompostBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED = SynchedEntityData.m_135353_(CompostBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.compost_superman.idle");
    private static final RawAnimation FLOAT_MOVE = RawAnimation.begin().thenLoop("animation.compost_superman.float_move");
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.compost_superman.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.compost_superman.turn_r");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.compost_superman.walk");
    private static final RawAnimation FIRE = RawAnimation.begin().thenPlay("animation.compost_superman.cannon_fire");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.compost_superman.gas_charge");
    private static final RawAnimation SHIFT = RawAnimation.begin().thenPlay("animation.compost_superman.phase_shift");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.compost_superman.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.compost_superman.death");
    private static final RawAnimation TRANSFORM = RawAnimation.begin().thenPlay("animation.compost_superman.transform");
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.compost_superman.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.compost_superman.signature_self_cast");
    private static final double T_FIRE = 32.0;
    private static final double T_CHARGE = 52.0;
    private static final double T_SHIFT = 44.0;
    private static final double T_TRANSFORM = 56.0;
    private static final double T_HURT = 9.0;
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final ServerBossEvent bossBar;
    private int burstLeft = 0;
    private int burstGap = 0;
    private int burstCd = 120;
    private int lobLeft = 0;
    private int lobGap = 0;
    private int gasCd = 200;
    private int lobCd = 160;
    private int gasFuse = -1;
    private int barrageCd = 90;
    private int barrageWindup = -1;
    private int curseWindup = -1;
    private int diveCd = 200;
    private int diveTicks = -1;
    private int laserCd = 120;
    private int laserTicks = -1;
    private int diveWindup = -1;
    private int orbitDir = 1;
    private int bombCd = 20;
    private int bombWindup = -1;
    private int dashTicks = 0;
    private int dashCd = 20;
    private int minionCd = 200;
    private int flyCd = 180;
    private int maggotCd = 140;
    private int slimeCd = 160;
    private int dungCd = 120;
    private int curseCd = 200;
    private double dashX = 0.0;
    private double dashZ = 0.0;
    private int hitStreak = 0;
    private int lastHitTick = -1000;
    private double actionTicks = 0.0;
    private String actionName = "";

    public CompostBossEntity(EntityType<? extends CompostBossEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 120;
        this.m_21530_();
        this.bossBar = new ServerBossEvent(this.m_5446_(), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
        if (!level.f_46443_) {
            this.ensureWeapon();
        }
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 340.0).m_22268_(Attributes.f_22284_, 2.0).m_22268_(Attributes.f_22281_, 4.0).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22277_, 32.0).m_22268_(Attributes.f_22278_, 0.4);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.0, false));
        this.f_21345_.m_25352_(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 16.0f));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_PHASE, (Object)1);
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_EMPOWERED, (Object)false);
    }

    public int getPhase() {
        return (Integer)this.f_19804_.m_135370_(DATA_PHASE);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public boolean isEmpowered() {
        return (Boolean)this.f_19804_.m_135370_(DATA_EMPOWERED);
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_("phase", this.getPhase());
        tag.m_128379_("empowered", this.isEmpowered());
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("phase")) {
            this.f_19804_.m_135381_(DATA_PHASE, (Object)tag.m_128451_("phase"));
        }
        if (tag.m_128441_("empowered")) {
            this.f_19804_.m_135381_(DATA_EMPOWERED, (Object)tag.m_128471_("empowered"));
        }
        if (!this.m_9236_().f_46443_) {
            this.ensureWeapon();
        }
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(RegularBossCastHost.action(kind), RegularBossCastHost.visualTicks(castTicks));
    }

    public void playAction(String name, double ticks) {
        this.actionState.start(name, (int)ticks);
        this.actionName = name;
        this.actionTicks = ticks;
    }

    public boolean m_6469_(DamageSource source, float amount) {
        boolean hit = super.m_6469_(source, amount);
        if (hit && this.actionTicks <= 0.0 && !this.m_21224_()) {
            this.playAction("hurt", 9.0);
        }
        return hit;
    }

    private void ensureWeapon() {
        ItemStack weapon = this.m_21205_();
        if (!weapon.m_150930_((Item)FTBossItems.COMPOST_CROWN.get())) {
            weapon = new ItemStack((ItemLike)FTBossItems.COMPOST_CROWN.get());
            this.m_21008_(InteractionHand.MAIN_HAND, weapon);
        }
        if (!weapon.m_41784_().m_128425_("AttributeModifiers", 9)) {
            weapon.m_41784_().m_128365_("AttributeModifiers", (Tag)new ListTag());
        }
        this.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
    }

    public void m_8119_() {
        Level level;
        ServerPlayer victim;
        LivingEntity sp2;
        Level n7;
        Level n22;
        Level n32;
        Level n42;
        Level n52;
        Level amp2;
        Level level2;
        boolean fly;
        int want;
        float ratio;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (!this.m_6084_()) {
            this.cancelPendingSkills();
            return;
        }
        if (this.m_5448_() == null || !this.m_5448_().m_6084_() || !BossTargets.valid(this.m_5448_())) {
            this.cancelPendingSkills();
        }
        if (this.actionTicks > 0.0) {
            this.actionTicks -= 1.0;
        }
        this.ensureWeapon();
        float f = ratio = this.m_21233_() > 0.0f ? this.m_21223_() / this.m_21233_() : 1.0f;
        int n6 = ratio <= 0.35f ? 3 : (want = ratio <= 0.7f ? 2 : 1);
        if (want > this.getPhase()) {
            Level level3;
            this.cancelPendingSkills();
            this.f_19804_.m_135381_(DATA_PHASE, (Object)want);
            if (want >= 2 && (level3 = this.m_9236_()) instanceof ServerLevel) {
                ServerLevel p2sl = (ServerLevel)level3;
                BossMusic.request(p2sl, (LivingEntity)this);
            }
            this.f_19804_.m_135381_(DATA_EMPOWERED, (Object)(want >= 2 ? 1 : 0));
            if (want == 2) {
                this.playAction("transform", 56.0);
            } else {
                this.playAction("phase_shift", 44.0);
            }
            level3 = this.m_9236_();
            if (level3 instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level3;
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12563_, SoundSource.HOSTILE, 1.4f, 0.7f);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 24, (ParticleOptions)ParticleTypes.f_123748_, 0.8, 0.05);
            }
            FTBossMod.LOGGER.info("[FTBoss]  {}", (Object)want);
        }
        boolean bl = fly = this.getPhase() >= 2;
        if (this.m_20068_() != fly) {
            this.m_20242_(fly);
        }
        if (fly && this.diveTicks <= 0 && (level2 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel fsl = (ServerLevel)level2;
            if (this.m_5448_() != null) {
                LivingEntity look = this.m_5448_();
                this.m_21563_().m_24960_((Entity)look, 30.0f, 30.0f);
                float wantYaw = (float)Math.toDegrees(Math.atan2(look.m_20189_() - this.m_20189_(), look.m_20185_() - this.m_20185_())) - 90.0f;
                this.m_146922_(wantYaw);
                this.f_20883_ = wantYaw;
                this.f_20885_ = wantYaw;
            }
            BlockPos gp = fsl.m_5452_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.m_20183_());
            double wantY = (double)gp.m_123342_() + 5.0 + Math.sin((double)this.f_19797_ * 0.08) * 0.3;
            double cy = this.m_20186_();
            this.m_20334_(0.0, 0.0, 0.0);
            BossCollision.move((Entity)this, new Vec3(0.0, Math.max(-0.3, Math.min(0.3, wantY - cy)), 0.0));
            if (this.m_5448_() != null) {
                double step;
                LivingEntity mt = this.m_5448_();
                this.orbitDir = this.f_19797_ / 40 % 2 == 0 ? 1 : -1;
                double ang = (double)this.f_19797_ * 0.045 * (double)this.orbitDir;
                double radius = 6.5 + Math.sin((double)this.f_19797_ * 0.02) * 2.0;
                double tx = mt.m_20185_() + Math.cos(ang) * radius;
                double tz = mt.m_20189_() + Math.sin(ang) * radius;
                double dx = tx - this.m_20185_();
                double dz = tz - this.m_20189_();
                double dl = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
                if (this.dashTicks <= 0) {
                    --this.dashCd;
                    if (this.dashCd <= 0) {
                        this.dashCd = 26 + this.f_19796_.m_188503_(14);
                        this.dashTicks = 14;
                        this.dashX = -Math.sin(Math.toRadians(this.m_146908_()));
                        this.dashZ = Math.cos(Math.toRadians(this.m_146908_()));
                    }
                }
                if (this.dashTicks > 0) {
                    --this.dashTicks;
                    step = 0.95;
                    BossCollision.move((Entity)this, new Vec3(this.dashX * step, 0.0, this.dashZ * step));
                } else {
                    step = Math.min(0.55, dl);
                }
                BossCollision.move((Entity)this, new Vec3(dx / dl * step, 0.0, dz / dl * step));
                if (--this.bombCd <= 0 && this.canStartSkill() && this.gasCd > 0 && this.lobCd > 0 && this.burstCd > 0 && this.barrageCd > 0 && this.diveCd > 0 && this.curseCd > 0) {
                    this.bombCd = 30;
                    this.playAction("cannon_fire", 32.0);
                    this.bombWindup = 17;
                }
                if (this.bombWindup > 0 && --this.bombWindup == 0) {
                    this.dropBomb(mt, fsl);
                }
            }
        }
        if (fly && this.m_5448_() != null && this.m_5448_().m_6084_()) {
            Level wantY;
            LivingEntity ft = this.m_5448_();
            if (this.barrageCd > 0) {
                --this.barrageCd;
            }
            if (this.canStartSkill() && this.barrageCd <= 0) {
                this.barrageCd = 170 + this.f_19796_.m_188503_(60);
                this.playAction("cannon_fire", 32.0);
                this.barrageWindup = 17;
            }
            if (this.barrageWindup > 0 && --this.barrageWindup == 0 && (wantY = this.m_9236_()) instanceof ServerLevel) {
                ServerLevel bsl2 = (ServerLevel)wantY;
                for (int i = -2; i <= 2; ++i) {
                    RainbowPoopEntity poop = new RainbowPoopEntity((Level)bsl2, (LivingEntity)this);
                    poop.setRainbow(true);
                    poop.setDamage(9.0f + (float)this.getPhase() * 2.5f);
                    double yaw = Math.toRadians(this.m_146908_());
                    double fwx = -Math.sin(yaw);
                    double fwz = Math.cos(yaw);
                    double sx = this.m_20185_() + fwx * 1.1;
                    double sy = this.m_20186_() + 1.2;
                    double sz = this.m_20189_() + fwz * 1.1;
                    poop.m_6034_(sx, sy, sz);
                    double dx = ft.m_20185_() - sx + (double)i * 1.9;
                    double dy = ft.m_20188_() - sy;
                    double dz = ft.m_20189_() - sz + (double)i * 1.9;
                    double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                    poop.m_6686_(dx / len, Math.min(-0.15, dy / len), dz / len, 1.3f, 0.8f);
                    bsl2.m_7967_((Entity)poop);
                    this.muzzleFx(bsl2, sx, sy, sz, fwx, fwz);
                }
                bsl2.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12473_, SoundSource.HOSTILE, 1.2f, 1.1f);
            }
            if (this.laserTicks > 0) {
                --this.laserTicks;
                Level i = this.m_9236_();
                if (i instanceof ServerLevel) {
                    ServerLevel lsl = (ServerLevel)i;
                    double lx = ft.m_20185_() - this.m_20185_();
                    double ly = ft.m_20188_() - (this.m_20186_() + 1.4);
                    double lz = ft.m_20189_() - this.m_20189_();
                    double len = Math.max(0.1, Math.sqrt(lx * lx + ly * ly + lz * lz));
                    for (double d = 1.0; d <= Math.min(24.0, len + 1.0); d += 0.6) {
                        Fx.particles((Level)lsl, this.m_20185_() + lx / len * d, this.m_20186_() + 1.4 + ly / len * d, this.m_20189_() + lz / len * d, 1, (ParticleOptions)ParticleTypes.f_123810_, 0.03, 0.0);
                    }
                    if (this.laserTicks % 2 == 0) {
                        for (Player lp : lsl.m_6907_()) {
                            double cz;
                            double cy;
                            double cx;
                            double pz;
                            double py;
                            double px;
                            double t;
                            if (!lp.m_6084_() || (t = ((px = lp.m_20185_() - this.m_20185_()) * lx + (py = lp.m_20188_() - (this.m_20186_() + 1.4)) * ly + (pz = lp.m_20189_() - this.m_20189_()) * lz) / (len * len)) < 0.0 || t > 1.0 || !((cx = px - lx * t) * cx + (cy = py - ly * t) * cy + (cz = pz - lz * t) * cz <= 1.7)) continue;
                            lp.f_19802_ = 0;
                            lp.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 4.5f + (float)this.getPhase() * 1.0f);
                        }
                    }
                }
            }
            if (this.diveCd > 0) {
                --this.diveCd;
            }
            if (this.canStartSkill() && this.diveCd <= 0) {
                this.diveCd = 180 + this.f_19796_.m_188503_(60);
                this.diveWindup = 14;
                this.playAction("phase_shift", 44.0);
            }
            if (this.diveWindup > 0 && --this.diveWindup == 0) {
                this.diveTicks = 40;
            }
            if (this.diveTicks > 0) {
                Level level4;
                --this.diveTicks;
                double dx = ft.m_20185_() - this.m_20185_();
                double dy = ft.m_20188_() - this.m_20186_();
                double dz = ft.m_20189_() - this.m_20189_();
                double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                this.m_20334_(dx / len * 0.55, dy / len * 0.55, dz / len * 0.55);
                for (Player dp : this.m_9236_().m_6907_()) {
                    if (!BossTargets.valid(dp) || !(this.m_20280_((Entity)dp) < 3.24)) continue;
                    dp.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 9.0f + (float)this.getPhase() * 2.0f);
                    BossKnockback.knockback((LivingEntity)dp, 1.2, this.m_20185_() - dp.m_20185_(), this.m_20189_() - dp.m_20189_());
                    dp.m_20254_(3);
                    this.diveTicks = 0;
                }
                if (this.diveTicks == 0 && (level4 = this.m_9236_()) instanceof ServerLevel) {
                    ServerLevel dsl = (ServerLevel)level4;
                    Fx.particles((Level)dsl, this.m_20185_(), this.m_20186_(), this.m_20189_(), 24, (ParticleOptions)ParticleTypes.f_123753_, 1.6, 0.1);
                    dsl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 1.2f, 0.8f);
                }
            }
        }
        if (this.f_19797_ % 40 == 0) {
            int debuffs = 0;
            for (MobEffectInstance inst : this.m_21220_()) {
                if (inst.m_19544_().m_19486_()) continue;
                ++debuffs;
            }
            if (debuffs > 0) {
                int amp2 = Math.min(4, debuffs - 1);
                this.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 90, amp2, false, false, false));
                this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 90, Math.min(3, amp2), false, false, false));
                this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 90, Math.min(3, amp2), false, false, false));
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] absorbed {} debuffs -> buff amp {}", (Object)debuffs, (Object)amp2);
            }
        }
        if (this.getPhase() >= 2 && this.minionCd > 0 && --this.minionCd <= 0 && (amp2 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel msl = (ServerLevel)amp2;
            this.minionCd = 240;
            if (!SummonLimit.atCap((LivingEntity)this, PoopMinionEntity.class)) {
                int n52 = Math.min(3 + this.f_19796_.m_188503_(2), SummonLimit.room((LivingEntity)this, PoopMinionEntity.class));
                for (int i = 0; i < n52; ++i) {
                    PoopMinionEntity m = (PoopMinionEntity)((EntityType)FTEntities.POOP_MINION.get()).m_20615_((Level)msl);
                    if (m == null) continue;
                    m.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.0, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.0, 0.0f, 0.0f);
                    if (this.m_5448_() != null) {
                        m.m_6710_(this.m_5448_());
                    }
                    msl.m_7967_((Entity)m);
                }
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} poop minions", (Object)n52);
            }
        }
        if (this.getPhase() >= 2 && this.flyCd > 0 && --this.flyCd <= 0 && (n52 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel fsl = (ServerLevel)n52;
            this.flyCd = 200 + this.f_19796_.m_188503_(80);
            if (!SummonLimit.atCap((LivingEntity)this, FlyMinionEntity.class)) {
                int n42 = Math.min(4 + this.f_19796_.m_188503_(3), SummonLimit.room((LivingEntity)this, FlyMinionEntity.class));
                for (int i = 0; i < n42; ++i) {
                    FlyMinionEntity f2 = (FlyMinionEntity)((EntityType)FTEntities.FLY_MINION.get()).m_20615_((Level)fsl);
                    if (f2 == null) continue;
                    f2.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 2.5, this.m_20186_() + 1.6 + this.f_19796_.m_188500_() * 1.2, this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 2.5, this.f_19796_.m_188501_() * 360.0f, 0.0f);
                    if (this.m_5448_() != null) {
                        f2.m_6710_(this.m_5448_());
                    }
                    fsl.m_7967_((Entity)f2);
                }
                fsl.m_6263_(null, this.m_20185_(), this.m_20186_() + 1.5, this.m_20189_(), SoundEvents.f_11690_, SoundSource.HOSTILE, 1.1f, 1.6f);
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} flies", (Object)n42);
            }
        }
        if (this.maggotCd > 0) {
            --this.maggotCd;
        }
        if (this.maggotCd <= 0 && (n42 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel gsl = (ServerLevel)n42;
            if (this.m_5448_() != null && this.m_5448_().m_6084_()) {
                this.maggotCd = 170 + this.f_19796_.m_188503_(80);
                if (!SummonLimit.atCap((LivingEntity)this, MaggotMinionEntity.class)) {
                    int n32 = Math.min(3 + this.f_19796_.m_188503_(3), SummonLimit.room((LivingEntity)this, MaggotMinionEntity.class));
                    for (int i = 0; i < n32; ++i) {
                        MaggotMinionEntity g = (MaggotMinionEntity)((EntityType)FTEntities.MAGGOT_MINION.get()).m_20615_((Level)gsl);
                        if (g == null) continue;
                        g.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                        g.m_6710_(this.m_5448_());
                        MinionBlessing.mark((Entity)g, this.m_20148_());
                        gsl.m_7967_((Entity)g);
                    }
                    FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} maggots", (Object)n32);
                }
            }
        }
        if (this.slimeCd > 0) {
            --this.slimeCd;
        }
        if (this.slimeCd <= 0 && (n32 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel ssl = (ServerLevel)n32;
            if (this.m_5448_() != null && this.m_5448_().m_6084_()) {
                this.slimeCd = 220 + this.f_19796_.m_188503_(90);
                if (!SummonLimit.atCap((LivingEntity)this, CompostSlimeEntity.class)) {
                    int n22 = Math.min(3 + this.f_19796_.m_188503_(2), SummonLimit.room((LivingEntity)this, CompostSlimeEntity.class));
                    for (int i = 0; i < n22; ++i) {
                        CompostSlimeEntity s = (CompostSlimeEntity)((EntityType)FTEntities.COMPOST_SLIME.get()).m_20615_((Level)ssl);
                        if (s == null) continue;
                        s.m_7839_(2 + this.f_19796_.m_188503_(3), true);
                        s.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                        s.m_6710_(this.m_5448_());
                        ssl.m_7967_((Entity)s);
                    }
                    FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} compost slimes", (Object)n22);
                }
            }
        }
        if (this.dungCd > 0) {
            --this.dungCd;
        }
        if (this.dungCd <= 0 && (n22 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel dsl = (ServerLevel)n22;
            if (this.m_5448_() != null && this.m_5448_().m_6084_()) {
                this.dungCd = 200 + this.f_19796_.m_188503_(80);
                if (!SummonLimit.atCap((LivingEntity)this, SheepDungEntity.class)) {
                    int n7 = Math.min(3 + this.f_19796_.m_188503_(3), SummonLimit.room((LivingEntity)this, SheepDungEntity.class));
                    for (int i = 0; i < n7; ++i) {
                        SheepDungEntity d = (SheepDungEntity)((EntityType)FTEntities.SHEEP_DUNG.get()).m_20615_((Level)dsl);
                        if (d == null) continue;
                        d.m_7839_(1 + this.f_19796_.m_188503_(2), true);
                        d.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_() + 0.4, this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                        d.m_6710_(this.m_5448_());
                        dsl.m_7967_((Entity)d);
                    }
                    dsl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12392_, SoundSource.HOSTILE, 1.0f, 1.3f);
                    FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} sheep dung", (Object)n7);
                }
            }
        }
        if (this.curseCd > 0) {
            --this.curseCd;
        }
        if (this.curseCd <= 0 && this.canStartSkill() && (n7 = this.m_9236_()) instanceof ServerLevel) {
            ServerPlayer sp2;
            LivingEntity i;
            ServerLevel csl = (ServerLevel)n7;
            this.curseCd = 200 + this.f_19796_.m_188503_(120);
            if (this.m_5448_() != null && this.m_5448_().m_6084_() && (i = this.m_5448_()) instanceof ServerPlayer && this.m_20280_((Entity)(sp2 = (ServerPlayer)i)) <= 1024.0 && this.f_19796_.m_188501_() < 0.35f) {
                this.playAction("cannon_fire", 32.0);
                this.curseWindup = 17;
                csl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12388_, SoundSource.HOSTILE, 1.2f, 0.6f);
            }
        }
        if (this.curseWindup > 0 && --this.curseWindup == 0 && (sp2 = this.m_5448_()) instanceof ServerPlayer && BossTargets.valid((Player)(victim = (ServerPlayer)sp2)) && this.m_142582_((Entity)victim) && this.m_20280_((Entity)victim) <= 1024.0) {
            PoopCurse.castOnce(victim, "\u7b54\u8fa9\u8d85\u4eba");
        }
        if (this.f_19797_ - this.lastHitTick > 60) {
            this.hitStreak = 0;
        }
        if (this.lobCd > 0) {
            --this.lobCd;
        }
        if (this.lobCd <= 0 && this.canStartSkill()) {
            this.lobCd = 140 + this.f_19796_.m_188503_(80);
            this.playAction("cannon_fire", 32.0);
            this.lobLeft = 3;
            this.lobGap = 17;
        }
        if (this.lobLeft > 0 && --this.lobGap <= 0) {
            this.lobGap = 4;
            this.fireLob(3 - this.lobLeft);
            --this.lobLeft;
        }
        if (this.gasCd > 0) {
            --this.gasCd;
        }
        if (this.gasCd <= 0 && this.canStartSkill()) {
            this.gasCd = 200 + this.f_19796_.m_188503_(100);
            this.playAction("gas_charge", 52.0);
            this.gasFuse = 41;
        }
        if (this.gasFuse > 1 && this.f_19797_ % 4 == 0) {
            Fx.ring(this.m_9236_(), this.m_20185_(), this.m_20186_() + 0.1, this.m_20189_(), 8.0, 24, (ParticleOptions)ParticleTypes.f_123749_, 0.0);
        }
        if (this.gasFuse > 0 && --this.gasFuse == 0) {
            this.gasExplode();
        }
        if (this.burstCd > 0) {
            --this.burstCd;
        }
        if (this.burstLeft > 0) {
            if (--this.burstGap <= 0) {
                this.burstGap = Math.max(1, 3 - this.hitStreak / 4);
                --this.burstLeft;
                this.firePoop();
            }
        } else if (this.burstCd <= 0 && this.canStartSkill()) {
            this.burstCd = Math.max(16, 55 - this.hitStreak * 4) + this.f_19796_.m_188503_(12);
            this.burstLeft = 4 + this.f_19796_.m_188503_(2);
            this.burstGap = 16;
            this.playAction("cannon_fire", 32.0);
        }
        if (this.bossBar != null) {
            this.bossBar.m_142711_(Math.max(0.0f, Math.min(1.0f, ratio)));
            BossBarVisibility.visible(this.bossBar, this.m_6084_());
            for (Player p : this.m_9236_().m_6907_()) {
                if (!(p instanceof ServerPlayer)) continue;
                ServerPlayer sp3 = (ServerPlayer)p;
                if (this.m_20280_((Entity)sp3) <= 2304.0) {
                    BossBarVisibility.addPlayer(this.bossBar, sp3);
                    continue;
                }
                this.bossBar.m_6539_(sp3);
            }
        }
        if ((level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel bsl = (ServerLevel)level;
            BossMusic.request(bsl, (LivingEntity)this);
        }
    }

    private boolean canStartSkill() {
        return this.actionTicks <= 0.0 && this.diveTicks <= 0 && this.diveWindup <= 0 && this.burstLeft <= 0 && this.lobLeft <= 0 && this.m_5448_() != null && this.m_5448_().m_6084_() && BossTargets.valid(this.m_5448_());
    }

    private void cancelPendingSkills() {
        this.burstLeft = 0;
        this.lobLeft = 0;
        this.gasFuse = -1;
        this.bombWindup = -1;
        this.barrageWindup = -1;
        this.curseWindup = -1;
        this.diveWindup = -1;
        this.diveTicks = -1;
    }

    public void noteHitOnPlayer() {
        this.hitStreak = Math.min(12, this.hitStreak + 1);
        this.lastHitTick = this.f_19797_;
    }

    public int getHitStreak() {
        return this.hitStreak;
    }

    private void dropBomb(LivingEntity target, ServerLevel sl) {
        RainbowPoopEntity bomb = new RainbowPoopEntity((Level)sl, (LivingEntity)this);
        bomb.setRainbow(this.getPhase() >= 2);
        bomb.setDamage(10.0f + (float)this.getPhase() * 3.0f);
        bomb.m_6034_(this.m_20185_(), this.m_20186_() - 0.4, this.m_20189_());
        double dx = target.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 2.0 - this.m_20185_();
        double dz = target.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 2.0 - this.m_20189_();
        double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        bomb.m_6686_(dx / dl * 0.95, 1.05, dz / dl * 0.95, 1.15f, 0.45f);
        sl.m_7967_((Entity)bomb);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12473_, SoundSource.HOSTILE, 0.8f, 0.7f);
    }

    private void fireLob(int index) {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        LivingEntity t = this.m_5448_();
        if (t == null) {
            return;
        }
        RainbowPoopEntity poop = new RainbowPoopEntity((Level)sl, (LivingEntity)this);
        poop.setRainbow(this.getPhase() >= 2);
        poop.setDamage(7.0f + (float)this.getPhase() * 2.5f);
        double yaw = Math.toRadians(this.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double sx = this.m_20185_() + fx * 1.2;
        double sy = this.m_20186_() + 2.1;
        double sz = this.m_20189_() + fz * 1.2;
        poop.m_6034_(sx, sy, sz);
        double dx = t.m_20185_() - sx + (double)(index - 1) * 1.2;
        double dy = t.m_20188_() - sy;
        double dz = t.m_20189_() - sz + (double)(index - 1) * 1.2;
        double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        poop.m_6686_(dx / len, 0.55 + dy / Math.max(4.0, len * 2.0), dz / len, 1.05f, 1.2f);
        sl.m_7967_((Entity)poop);
        sl.m_6263_(null, sx, sy, sz, SoundEvents.f_12473_, SoundSource.HOSTILE, 1.0f, 1.2f);
        this.muzzleFx(sl, sx, sy, sz, fx, fz);
    }

    private void gasExplode() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        double x = this.m_20185_();
        double y = this.m_20186_() + 0.6;
        double z = this.m_20189_();
        sl.m_254849_((Entity)this, x, y, z, 1.6f, Level.ExplosionInteraction.NONE);
        sl.m_6263_(null, x, y, z, SoundEvents.f_11913_, SoundSource.HOSTILE, 1.8f, 0.6f);
        Fx.particles((Level)sl, x, y, z, 60, (ParticleOptions)ParticleTypes.f_123753_, 2.6, 0.15);
        Fx.particles((Level)sl, x, y, z, 20, (ParticleOptions)ParticleTypes.f_123755_, 2.0, 0.08);
        for (Player p : sl.m_6907_()) {
            if (!(p.m_20280_((Entity)this) <= 64.0) || !p.m_6084_()) continue;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 8.0f + (float)this.getPhase() * 1.5f);
            p.m_20254_(4);
        }
        FTBossMod.LOGGER.info("[FTBoss][CompostMan] gas explosion at {}, {}, {}", new Object[]{(int)x, (int)y, (int)z});
    }

    private void firePoop() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        LivingEntity t = this.m_5448_();
        RainbowPoopEntity poop = new RainbowPoopEntity((Level)sl, (LivingEntity)this);
        poop.setRainbow(this.getPhase() >= 2);
        poop.setDamage(6.0f + (float)this.getPhase() * 2.0f);
        double yaw = Math.toRadians(this.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = Math.cos(yaw);
        double rz = Math.sin(yaw);
        double sx = this.m_20185_() + rx * 1.15 + fx * 1.1;
        double sy = this.m_20186_() + 1.85 + (this.actionTicks > 0.0 ? 0.35 : 0.0);
        double sz = this.m_20189_() + rz * 1.15 + fz * 1.1;
        poop.m_6034_(sx, sy, sz);
        if (t != null) {
            double dx = t.m_20185_() - sx;
            double dy = t.m_20188_() - sy;
            double dz = t.m_20189_() - sz;
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            double lead = Math.min(1.0, len / 24.0);
            double len2 = Math.max(0.1, Math.sqrt((dx += t.m_20184_().f_82479_ * lead * 12.0) * dx + (dy += t.m_20184_().f_82480_ * lead * 6.0) * dy + (dz += t.m_20184_().f_82481_ * lead * 12.0) * dz));
            poop.m_6686_(dx / len2, dy / len2 + 0.1, dz / len2, 1.35f, 0.6f);
        } else {
            poop.m_6686_(0.0, 0.0, 0.0, 0.0f, 0.0f);
        }
        sl.m_7967_((Entity)poop);
        sl.m_6263_(null, sx, sy, sz, SoundEvents.f_12473_, SoundSource.HOSTILE, 1.0f, 1.4f);
        this.muzzleFx(sl, sx, sy, sz, fx, fz);
    }

    private void muzzleFx(ServerLevel sl, double x, double y, double z, double fx, double fz) {
        Fx.particles((Level)sl, x, y, z, 4, (ParticleOptions)ParticleTypes.f_123796_, 0.1, 0.03);
        Fx.particles((Level)sl, x + fx * 0.45, y + 0.05, z + fz * 0.45, 6, (ParticleOptions)ParticleTypes.f_123753_, 0.16, 0.08);
        Fx.particles((Level)sl, x + fx * 0.25, y + 0.12, z + fz * 0.25, 2, (ParticleOptions)ParticleTypes.f_123777_, 0.06, 0.012);
    }

    public boolean m_6051_() {
        return false;
    }

    public boolean m_6785_(double distance) {
        return false;
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            BossMusic.release(sl, (LivingEntity)this);
        }
        SummonSweep.clear((LivingEntity)this, 80.0, PoopMinionEntity.class, FlyMinionEntity.class, CompostSlimeEntity.class, SheepDungEntity.class);
        SummonSweep.clearOwned((LivingEntity)this, 80.0);
    }

    public void m_142687_(Entity.RemovalReason reason) {
        if (!this.m_9236_().f_46443_) {
            BossBars.untrack((Entity)this, this.bossBar);
        }
        super.m_142687_(reason);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "compost_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<CompostBossEntity> state) {
        this.actionState.prepare(state);
        this.signatureCast.prepare(state);
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (this.signatureCast.active()) {
            state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST);
            return PlayState.CONTINUE;
        }
        if (this.actionState.active()) {
            switch (this.actionState.name()) {
                case "cannon_fire": {
                    state.setAnimation(FIRE);
                    return PlayState.CONTINUE;
                }
                case "gas_charge": {
                    state.setAnimation(CHARGE);
                    return PlayState.CONTINUE;
                }
                case "phase_shift": {
                    state.setAnimation(SHIFT);
                    return PlayState.CONTINUE;
                }
                case "transform": {
                    state.setAnimation(TRANSFORM);
                    return PlayState.CONTINUE;
                }
            }
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        boolean flying = this.getPhase() >= 2;
        float dYaw = Mth.m_14177_((float)(this.m_146908_() - this.f_19859_));
        if (Math.abs(dYaw) > 1.2f) {
            state.setAnimation(dYaw > 0.0f ? TURN_L : TURN_R);
            return PlayState.CONTINUE;
        }
        state.setAnimation(flying ? FLOAT_MOVE : (state.isMoving() ? WALK : IDLE));
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

