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
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityDimensions
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossMeleeWindup;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossUnstuck;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.RatSwarmEncounter;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.boss.ai.ratswarm.RatSwarmCombat;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RatBossEntity
extends Monster
implements GeoAnimatable {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(RatBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private final BossMeleeWindup meleeWindup = new BossMeleeWindup((Mob)this, this.actionState, 5, 10, 1.8);
    private boolean swarmPending;
    private static final EntityDataAccessor<Boolean> DATA_MINION = SynchedEntityData.m_135353_(RatBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private static final double BOSS_BAR_RANGE = 48.0;
    private static final String MINION_TAG = "rat_minion";
    private final ServerBossEvent bossBar;
    private boolean musicStarted = false;
    private int swarmCd = 60;
    private int cheeseCd = 40;
    private MobEffect cheeseEffect;
    private boolean cheeseLogged = false;
    private static final Set<UUID> LEADERS = ConcurrentHashMap.newKeySet();

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_MINION, (Object)false);
    }

    /*
     * WARNING - void declaration
     */
    public boolean m_7327_(Entity entity) {
        void target;
        if (RatSwarmEncounter.isMaster(this)) {
            return false;
        }
        if (RatSwarmCombat.tryMelee(this, entity, this.actionState)) {
            return false;
        }
        this.meleeWindup.start((Entity)target);
        return false;
    }

    private boolean biteTarget(LivingEntity target) {
        boolean hit = super.m_7327_((Entity)target);
        if (hit && !this.m_9236_().f_46443_) {
            this.m_5634_(1.0f);
            RatSwarmCombat.bite(this, target);
        }
        return hit;
    }

    public boolean m_6051_() {
        return false;
    }

    public RatBossEntity(EntityType<? extends RatBossEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 120;
        this.bossBar = new ServerBossEvent((Component)Component.m_237115_((String)"boss.ftboss.rat_swarm"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
    }

    public void hideBossBar() {
        BossBarVisibility.visible(this.bossBar, false);
        this.bossBar.m_7706_();
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public boolean isMinion() {
        return (Boolean)this.f_19804_.m_135370_(DATA_MINION);
    }

    public void setMinion(boolean v) {
        this.f_19804_.m_135381_(DATA_MINION, (Object)v);
        AttributeInstance movement = this.m_21051_(Attributes.f_22279_);
        if (movement != null) {
            movement.m_22100_(v ? 0.36 : 0.1);
        }
        this.getPersistentData().m_128379_(MINION_TAG, v);
        this.m_6210_();
    }

    public void m_7350_(EntityDataAccessor<?> accessor) {
        super.m_7350_(accessor);
        if (DATA_MINION.equals(accessor)) {
            this.m_6210_();
        }
    }

    public EntityDimensions m_6972_(Pose pose) {
        return this.isMinion() ? EntityDimensions.m_20395_((float)0.4f, (float)0.6f) : EntityDimensions.m_20395_((float)0.6f, (float)0.975f);
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128379_(MINION_TAG, this.isMinion());
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "rat_move", 4, state -> {
            this.actionState.prepare(state);
            if (RatSwarmCombat.animate(this, this.actionState, state)) {
                return PlayState.CONTINUE;
            }
            if (this.m_21224_()) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.startle"));
                return PlayState.CONTINUE;
            }
            if ("melee".equals(this.actionState.name())) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.bite"));
                return PlayState.CONTINUE;
            }
            if ("call".equals(this.actionState.name())) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.call"));
                return PlayState.CONTINUE;
            }
            if (this.f_20916_ > 0) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.startle"));
                return PlayState.CONTINUE;
            }
            if (!this.m_20096_()) {
                state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.jump"));
                return PlayState.CONTINUE;
            }
            double spd = this.m_20184_().m_165925_();
            if (spd > 0.02) {
                state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.run"));
            } else if (spd > 8.0E-4) {
                state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.walk"));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.idle"));
            }
            return PlayState.CONTINUE;
        })});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.setMinion(tag.m_128471_(MINION_TAG));
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 300.0).m_22268_(Attributes.f_22284_, 2.5).m_22268_(Attributes.f_22281_, 4.5).m_22268_(Attributes.f_22283_, 1.6).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 40.0);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(1, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.3, true));
        this.f_21345_.m_25352_(2, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 10.0f));
        this.f_21345_.m_25352_(3, (Goal)new RandomStrollGoal((PathfinderMob)this, 0.9));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    /*
     * WARNING - void declaration
     */
    public boolean m_6469_(DamageSource damageSource, float f) {
        void source;
        float amount;
        if (RatSwarmEncounter.blocksDamage(this, damageSource, f)) {
            return false;
        }
        if (!this.isMinion() && (amount = BossDamageGate.gate((LivingEntity)this, (DamageSource)source, amount)) <= 0.0f) {
            return false;
        }
        boolean hit = super.m_6469_((DamageSource)source, amount);
        if (hit && !this.isMinion()) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        if (hit && !this.isMinion()) {
            BossBarVisibility.visible(this.bossBar, true);
        }
        return hit;
    }

    public void m_8119_() {
        super.m_8119_();
        if (RatSwarmEncounter.tick(this)) {
            return;
        }
        if (this.m_9236_().f_46443_) {
            RatSwarmCombat.tick(this, this.actionState);
            return;
        }
        this.meleeWindup.tick(this::biteTarget);
        if (!this.isMinion()) {
            BossDamageGate.tick((LivingEntity)this);
            BossRage.tickBoss((LivingEntity)this);
            TrapBreaker.tick((LivingEntity)this);
            BossUnstuck.tick((LivingEntity)this);
            MinionBlessing.tick((LivingEntity)this, 40.0);
            this.tickMusic();
        }
        if (this.isMinion()) {
            this.hideBossBar();
            if (this.f_19797_ % 20 == 0) {
                boolean hasLeader = LemonSacrifice.ratLeader((Entity)this) != null;
                Iterator<UUID> it = LEADERS.iterator();
                while (it.hasNext()) {
                    RatBossEntity r;
                    Entity e;
                    UUID u = it.next();
                    Level level = this.m_9236_();
                    if (level instanceof ServerLevel) {
                        ServerLevel sl = (ServerLevel)level;
                        v0 = sl.m_8791_(u);
                    } else {
                        v0 = e = null;
                    }
                    if (e == null) {
                        it.remove();
                        continue;
                    }
                    if (!(e instanceof RatBossEntity) || (r = (RatBossEntity)e).isMinion() || !r.m_6084_()) continue;
                    hasLeader = true;
                }
                if (!hasLeader) {
                    this.m_6074_();
                    RatSwarmCombat.tick(this, this.actionState);
                    return;
                }
            }
            for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
                if (inst.m_19544_().m_19486_()) continue;
                this.m_21195_(inst.m_19544_());
            }
            RatSwarmCombat.tick(this, this.actionState);
            return;
        }
        if (this.f_19797_ % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
                if (inst.m_19544_().m_19486_()) continue;
                this.m_21195_(inst.m_19544_());
            }
        }
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.bossBar);
            RatSwarmCombat.tick(this, this.actionState);
            return;
        }
        LEADERS.add(this.m_20148_());
        this.bossBar.m_142711_(this.m_21223_() / this.m_21233_());
        boolean want = false;
        for (ServerPlayer p : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (this.m_20280_((Entity)p) < 2304.0) {
                BossBarVisibility.addPlayer(this.bossBar, p);
                want = true;
                continue;
            }
            this.bossBar.m_6539_(p);
        }
        if (this.bossBar.m_8323_() != want) {
            BossBarVisibility.visible(this.bossBar, want);
        }
        if (--this.swarmCd <= 0 && !this.actionState.active()) {
            this.swarmCd = (double)this.m_21223_() < (double)this.m_21233_() * 0.5 ? 65 : 100;
            this.actionState.start("call", 18);
            this.swarmPending = true;
        }
        if (this.swarmPending && this.actionState.elapsed(0.0f) >= 9.0f) {
            this.swarmPending = false;
            this.maintainSwarm();
            Fx.ring(this.m_9236_(), this.m_20185_(), this.m_20186_() + 0.1, this.m_20189_(), 3.0, 20, (ParticleOptions)ParticleTypes.f_123759_, 0.02);
        }
        if (--this.cheeseCd <= 0) {
            this.cheeseCd = 160;
            this.applyCheeseBuff();
        }
        RatSwarmCombat.tick(this, this.actionState);
    }

    private void maintainSwarm() {
        int alive = 0;
        for (RatBossEntity r : this.m_9236_().m_45976_(RatBossEntity.class, this.m_20191_().m_82400_(48.0))) {
            if (!r.isMinion() || !r.m_6084_() || !SummonSweep.belongsTo((Entity)this, (Entity)r)) continue;
            ++alive;
            r.m_6710_(this.m_5448_());
            r.m_21530_();
        }
        int cap = (double)this.m_21223_() < (double)this.m_21233_() * 0.5 ? 16 : 12;
        int spawned = 0;
        for (int i = alive; i < cap && i < alive + 6; ++i) {
            RatBossEntity baby = (RatBossEntity)this.m_6095_().m_20615_(this.m_9236_());
            if (baby == null) continue;
            double a = Math.random() * Math.PI * 2.0;
            double r = 2.5 + Math.random() * 4.0;
            baby.setMinion(true);
            MinionBlessing.mark((Entity)baby, this.m_20148_());
            baby.m_6034_(this.m_20185_() + Math.cos(a) * r, this.m_20186_(), this.m_20189_() + Math.sin(a) * r);
            baby.m_6593_((Component)Component.m_237115_((String)"entity.ftboss.rat_minion"));
            baby.m_20340_(true);
            baby.m_21530_();
            baby.m_21153_(baby.m_21233_());
            if (this.m_5448_() != null) {
                baby.m_6710_(this.m_5448_());
            }
            ManorFightMembers.inherit((Entity)this, (Entity)baby);
            RatSwarmCombat.spawn(this.m_9236_(), (Entity)baby, this);
            ++spawned;
        }
        if (spawned > 0 && this.m_21223_() < this.m_21233_()) {
            this.m_5634_(Math.min((float)spawned * 3.0f, this.m_21233_() - this.m_21223_()));
        }
        FTBossMod.LOGGER.info("[FTBoss] \u9f20\u7fa4\u9f20\u6f6e: \u5728\u573a\u5c0f\u8001\u9f20 {} \u53ea (\u672c\u8f6e\u53ec\u5524 {} \u53ea, \u516c\u5171\u8840\u6c60 {}/{})", new Object[]{alive + spawned, spawned, (int)this.m_21223_(), (int)this.m_21233_()});
    }

    private void applyCheeseBuff() {
        if (this.cheeseEffect == null) {
            for (MobEffect fx : ForgeRegistries.MOB_EFFECTS.getValues()) {
                ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey((Object)fx);
                if (key == null || !key.m_135815_().toLowerCase().contains("cheese") || !fx.m_19486_()) continue;
                this.cheeseEffect = fx;
                break;
            }
        }
        if (this.cheeseEffect != null) {
            this.m_7292_(new MobEffectInstance(this.cheeseEffect, 400, 0, false, false, true));
            if (!this.cheeseLogged) {
                this.cheeseLogged = true;
                FTBossMod.LOGGER.info("[FTBoss] \u9f20\u7fa4\u5976\u916abuff: {}", (Object)ForgeRegistries.MOB_EFFECTS.getKey((Object)this.cheeseEffect));
            }
        } else {
            this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 400, 0, false, false, true));
            if (!this.cheeseLogged) {
                this.cheeseLogged = true;
                FTBossMod.LOGGER.info("[FTBoss] \u9f20\u7fa4\u81ea\u8eabbuff: \u672a\u627e\u5230\u5976\u916a\u6548\u679c, \u4ec5\u4f7f\u7528 \u901f\u5ea6(2026-09-11 \u5df2\u6309\u7528\u6237\u8981\u6c42\u51cf\u91cf)");
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    public void m_6667_(DamageSource damageSource) {
        void source;
        RatSwarmEncounter.cleanup(this);
        BossBars.untrack((Entity)this, this.bossBar);
        LEADERS.remove(this.m_20148_());
        if (!this.m_9236_().f_46443_) {
            for (RatBossEntity r : this.m_9236_().m_45976_(RatBossEntity.class, this.m_20191_().m_82400_(64.0))) {
                if (!r.isMinion() || !r.m_6084_() || !RatSwarmEncounter.legacyBelongsTo((Entity)this, (Entity)r)) continue;
                r.m_6074_();
            }
        }
        super.m_6667_((DamageSource)source);
    }

    private void tickMusic() {
        if (this.isMinion()) {
            return;
        }
        if (!this.m_6084_()) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (!this.musicStarted) {
            if (sl.m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(40.0)).isEmpty()) {
                return;
            }
            this.musicStarted = true;
        }
        BossMusic.request(sl, (LivingEntity)this);
    }

    public void stopMusic() {
        if (!this.musicStarted) {
            return;
        }
        this.musicStarted = false;
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            BossMusic.release(sl, (LivingEntity)this);
        }
    }

    /*
     * WARNING - void declaration
     */
    public void m_142687_(Entity.RemovalReason removalReason) {
        void reason;
        RatSwarmEncounter.cleanupRemoved(this, removalReason);
        BossBars.untrack((Entity)this, this.bossBar);
        this.stopMusic();
        LEADERS.remove(this.m_20148_());
        if (!this.m_9236_().f_46443_ && !this.isMinion()) {
            for (RatBossEntity r : this.m_9236_().m_45976_(RatBossEntity.class, this.m_20191_().m_82400_(64.0))) {
                if (!r.isMinion() || !r.m_6084_() || !RatSwarmEncounter.legacyBelongsTo((Entity)this, (Entity)r)) continue;
                r.m_6074_();
            }
        }
        super.m_142687_((Entity.RemovalReason)reason);
    }

    protected boolean m_21527_() {
        return false;
    }

    public void tickEncounterMelee() {
        this.meleeWindup.tick(this::biteTarget);
    }
}

