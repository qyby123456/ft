/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob
 *  io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
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
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
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
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 */
package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.boss.ai.BarrelTactics;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossUnstuck;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.SmallBarrelEntity;
import net.farmingtales.ftboss.entity.WineBarrelDecoyEntity;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class WineBarrelBossEntity
extends AbstractSpellCastingMob
implements RegularBossCastHost {
    private static final double BOSS_BAR_RANGE = 48.0;
    private static final EntityDataAccessor<Integer> CROSSBOW_PULL_TICKS = SynchedEntityData.m_135353_(WineBarrelBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private int previousCrossbowPullTicks;
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(WineBarrelBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(WineBarrelBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, DATA_SIGNATURE_CAST);
    private static final int SLAM_TICKS = 24;
    private static final int SLAM_HIT_TICK = 10;
    private static final int SPRAY_TICKS = 50;
    private static final int SPRAY_HIT_TICK = 10;
    private int lastVisualState = 0;
    private String pendingVolley = "";
    private UUID volleyTarget;
    private MobEffect wineArmor;
    private MobEffect wineHealth;
    private MobEffect wineResist;
    private MobEffect wineLuck;
    private int auraTimer = 0;
    private int stage = 0;
    private int summonCd = 150;
    private int castCd = 40;
    private int fireballCd = 60;
    private int cannonCd = 140;
    private final List<AbstractSpell> spellQueue = new ArrayList<AbstractSpell>();
    private final List<UUID> minions = new ArrayList<UUID>();
    private final ServerBossEvent bossBar;
    private boolean musicStarted = false;
    private static final int ST_IDLE = 0;
    private static final int ST_WINDUP = 1;
    private static final int ST_CHARGE = 2;
    private static final int ST_SLAM = 3;
    private static final int ST_POP = 4;
    private int bState = 0;
    private int bTimer = 0;
    private int chargeCd = 110;
    private int popCd = 80;
    private int mistCd = 0;
    private double chargeDx = 0.0;
    private double chargeDz = 0.0;
    private final BarrelTactics tactics = new BarrelTactics((Mob)this);
    private static final RawAnimation A_IDLE = RawAnimation.begin().thenLoop("animation.barrel_boss.idle");
    private static final RawAnimation A_WINDUP = RawAnimation.begin().thenPlay("animation.barrel_boss.windup");
    private static final RawAnimation A_ROLL = RawAnimation.begin().thenLoop("animation.barrel_boss.roll");
    private static final RawAnimation A_POP = RawAnimation.begin().thenPlay("animation.barrel_boss.spray_attack");
    private static final RawAnimation A_SLAM = RawAnimation.begin().thenPlay("animation.barrel_boss.slam");
    private static final RawAnimation A_HURT = RawAnimation.begin().thenPlay("animation.barrel_boss.hurt");
    private static final RawAnimation A_DEATH = RawAnimation.begin().thenPlay("animation.barrel_boss.death");
    private static final RawAnimation A_SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.barrel_boss.signature_cast");
    private static final RawAnimation A_SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.barrel_boss.signature_self_cast");
    public static final int CROSSBOW_READY_LEAD = 20;
    private boolean crossbowReady = false;

    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (this.isCasting()) {
            return;
        }
        super.initiateCastSpell(spell, spellLevel);
        BossSpellTuning.fasterLegacyCast(this, spell);
    }

    public boolean m_6051_() {
        return false;
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(CROSSBOW_PULL_TICKS, (Object)0);
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, (Object)new CompoundTag());
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(RegularBossCastHost.action(kind), RegularBossCastHost.visualTicks(castTicks));
    }

    public float getCrossbowPull(float partialTick) {
        int current = (Integer)this.f_19804_.m_135370_(CROSSBOW_PULL_TICKS);
        if (current == 0) {
            return 0.0f;
        }
        return Mth.m_14179_((float)partialTick, (float)this.previousCrossbowPullTicks, (float)current) / 25.0f;
    }

    public WineBarrelBossEntity(EntityType<? extends WineBarrelBossEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 80;
        this.bossBar = new ServerBossEvent((Component)Component.m_237115_((String)"boss.ftboss.wine_barrel"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 400.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22281_, 5.5).m_22268_(Attributes.f_22284_, 3.0).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 40.0);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        super.registerControllers(registrar);
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "barrel_ctrl", 4, state -> {
            int visualState;
            this.actionState.prepare(state);
            this.signatureCast.prepare(state);
            int n = visualState = this.actionState.active() ? Integer.parseInt(this.actionState.name()) : 0;
            if (this.m_6084_() && this.signatureCast.active()) {
                state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? A_SIGNATURE_SELF : A_SIGNATURE_CAST);
                return PlayState.CONTINUE;
            }
            if (this.m_6084_() && visualState == 0 && this.isCasting()) {
                return PlayState.STOP;
            }
            double spd = this.m_20184_().m_165925_();
            if (this.m_21224_()) {
                if (!state.isCurrentAnimation(A_DEATH)) {
                    state.setAnimation(A_DEATH);
                }
            } else if (this.f_20916_ > 2 && visualState == 0 && !this.isCasting()) {
                if (!state.isCurrentAnimation(A_HURT)) {
                    state.setAnimation(A_HURT);
                }
            } else if (visualState == 1) {
                if (!state.isCurrentAnimation(A_WINDUP)) {
                    state.setAnimation(A_WINDUP);
                }
            } else if (visualState == 2 || visualState == 0 && spd > 0.012) {
                if (!state.isCurrentAnimation(A_ROLL)) {
                    state.setAnimation(A_ROLL);
                }
            } else if (visualState == 3) {
                if (!state.isCurrentAnimation(A_SLAM)) {
                    state.setAnimation(A_SLAM);
                }
            } else if (visualState == 4) {
                if (!state.isCurrentAnimation(A_POP)) {
                    state.setAnimation(A_POP);
                }
            } else if (!state.isCurrentAnimation(A_IDLE)) {
                state.setAnimation(A_IDLE);
            }
            return PlayState.CONTINUE;
        })});
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(1, (Goal)new MeleeAttackGoal((PathfinderMob)this, 0.0, false));
        this.f_21345_.m_25352_(2, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 16.0f));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public boolean m_6469_(DamageSource source, float amount) {
        boolean hit;
        boolean fireHit;
        if ((amount = BossDamageGate.gate((LivingEntity)this, source, amount)) <= 0.0f) {
            return false;
        }
        String mid = source.m_19385_();
        boolean bl = fireHit = mid.contains("fire") || mid.contains("lava") || mid.contains("burn") || mid.contains("blaze");
        if (!this.m_9236_().f_46443_ && fireHit && amount > 0.0f && this.m_21223_() < this.m_21233_()) {
            this.m_5634_(amount * 0.75f);
        }
        if (hit = super.m_6469_(source, amount)) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        if (hit) {
            BossBarVisibility.visible(this.bossBar, true);
            if (!this.m_9236_().f_46443_) {
                this.m_9236_().m_6263_(null, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), SoundEvents.f_12630_, SoundSource.HOSTILE, 0.9f, 0.6f);
            }
        }
        return hit;
    }

    public void m_8119_() {
        this.previousCrossbowPullTicks = (Integer)this.f_19804_.m_135370_(CROSSBOW_PULL_TICKS);
        super.m_8119_();
        if (!this.m_9236_().f_46443_) {
            BossDamageGate.tick((LivingEntity)this);
            BossRage.tickBoss((LivingEntity)this);
            TrapBreaker.tick((LivingEntity)this);
            BossUnstuck.tick((LivingEntity)this);
            this.tickPurge();
            this.tickWineAura();
            this.tickWineCombat();
            this.tickCrossbow();
            this.tickBarrelActions();
            if (this.bState != this.lastVisualState) {
                this.actionState.start(Integer.toString(this.bState), this.bTimer);
                this.lastVisualState = this.bState;
            }
            this.tickMusic();
        }
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.bossBar);
            return;
        }
        this.bossBar.m_142711_(this.m_21223_() / this.m_21233_());
        boolean wantVisible = false;
        for (ServerPlayer p : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (this.m_20280_((Entity)p) < 2304.0) {
                BossBarVisibility.addPlayer(this.bossBar, p);
                wantVisible = true;
                continue;
            }
            this.bossBar.m_6539_(p);
        }
        if (this.bossBar.m_8323_() != wantVisible) {
            BossBarVisibility.visible(this.bossBar, wantVisible);
        }
    }

    private void tickPurge() {
        if (this.f_19797_ % 20 != 0) {
            return;
        }
        for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
            if (inst.m_19544_().m_19486_()) continue;
            this.m_21195_(inst.m_19544_());
        }
    }

    private void tickWineAura() {
        if (++this.auraTimer < 10) {
            return;
        }
        this.auraTimer = 0;
        if (this.wineArmor == null) {
            this.wineArmor = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("vinery", "armor_effect"));
        }
        if (this.wineHealth == null) {
            this.wineHealth = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("vinery", "health_effect"));
        }
        this.keep(this.wineArmor, 0);
        this.keep(this.wineHealth, 1);
    }

    private void keep(MobEffect fx, int amp) {
        if (fx == null) {
            return;
        }
        MobEffectInstance cur = this.m_21124_(fx);
        if (cur == null || cur.m_19557_() < 120) {
            this.m_7292_(new MobEffectInstance(fx, 600, amp, false, false, true));
        }
    }

    private void applyStage() {
        MobEffect next;
        MobEffect[] all;
        int want;
        float ratio = this.m_21223_() / this.m_21233_();
        int n = (double)ratio < 0.34 ? 2 : (want = (double)ratio < 0.67 ? 1 : 0);
        if (want <= this.stage) {
            return;
        }
        this.stage = want;
        this.ensureWineEffects();
        for (MobEffect fx : all = new MobEffect[]{this.wineArmor, this.wineHealth, this.wineResist, this.wineLuck}) {
            MobEffectInstance cur;
            if (fx == null || (cur = this.m_21124_(fx)) == null) continue;
            this.m_7292_(new MobEffectInstance(fx, 600, Math.min(4, cur.m_19564_() + 1), false, false, true));
        }
        MobEffect mobEffect = next = want == 1 ? this.wineResist : this.wineLuck;
        if (next != null) {
            this.m_7292_(new MobEffectInstance(next, 600, want - 1, false, false, true));
        }
        FTBossMod.LOGGER.info("[FTBoss] \u9152\u9b54\u00b7\u9152\u6876\u738b \u9636\u6bb5{}: \u9152buff\u5347\u7ea7+\u65b0buff", (Object)want);
    }

    private void ensureWineEffects() {
        if (this.wineArmor == null) {
            this.wineArmor = this.find("armor_effect");
        }
        if (this.wineHealth == null) {
            this.wineHealth = this.find("health_effect");
        }
        if (this.wineResist == null) {
            this.wineResist = this.find("resistance_effect");
        }
        if (this.wineLuck == null) {
            this.wineLuck = this.find("luck_effect");
        }
    }

    private MobEffect find(String id) {
        return (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("vinery", id));
    }

    private void tickCrossbow() {
        boolean holding;
        int pullTicks = this.bState == 0 && this.m_6084_() ? Mth.m_14045_((int)(45 - this.cannonCd), (int)0, (int)25) : 0;
        this.f_19804_.m_135381_(CROSSBOW_PULL_TICKS, (Object)pullTicks);
        boolean wantReady = this.bState == 0 && this.cannonCd <= 20;
        ItemStack hand = this.m_6844_(EquipmentSlot.MAINHAND);
        boolean bl = holding = hand.m_150930_((Item)FTBossItems.BARREL_CROSSBOW.get()) || hand.m_150930_((Item)FTBossItems.BARREL_CROSSBOW_READY.get());
        if (wantReady == this.crossbowReady && holding) {
            return;
        }
        this.crossbowReady = wantReady;
        this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(wantReady ? (ItemLike)FTBossItems.BARREL_CROSSBOW_READY.get() : (ItemLike)FTBossItems.BARREL_CROSSBOW.get()));
        this.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
    }

    private void tickWineCombat() {
        if (this.m_21224_()) {
            return;
        }
        this.applyStage();
        LivingEntity target = BossTargets.combatTarget((Mob)this, 24.0);
        if (target == null) {
            return;
        }
        this.m_6710_(target);
        this.tickDrunkenAura();
        if (this.fireballCd > 0) {
            --this.fireballCd;
        }
        if (this.cannonCd > 0) {
            --this.cannonCd;
        }
        if (this.summonCd > 0) {
            --this.summonCd;
        }
        this.tickIronMagic(target);
        if (this.bState != 0) {
            return;
        }
        if (this.cannonCd <= 0) {
            this.cannonCd = this.stage >= 2 ? 110 : 170;
            this.startVolley("cannon", target);
            return;
        }
        if (this.summonCd <= 0) {
            this.summonCd = this.stage >= 1 ? 150 : 210;
            this.startVolley("summon", target);
            return;
        }
        if (this.fireballCd <= 0) {
            this.fireballCd = this.stage >= 2 ? 45 : 70;
            this.startVolley("fireball", target);
            return;
        }
    }

    private void tickIronMagic(LivingEntity target) {
        if (this.isCasting()) {
            return;
        }
        if (this.spellQueue.isEmpty()) {
            if (--this.castCd <= 0) {
                this.castCd = NonManorFinaleDifficulty.acceleratedTicks((LivingEntity)this, this.stage >= 2 ? 34 : 58);
                this.fillSpellQueue(target);
            }
        } else if (!this.isCasting()) {
            this.castNextSpell(target);
        }
    }

    private void tickDrunkenAura() {
        if (this.m_21525_() || !this.m_6084_()) {
            return;
        }
        if (this.f_19797_ % 30 == 0) {
            for (ServerPlayer p : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(7.0))) {
                if (!p.m_6084_() || p.m_7500_() || p.m_5833_() || this.m_7307_((Entity)p) || !(this.m_20280_((Entity)p) <= 49.0) || !this.m_142582_((Entity)p)) continue;
                MobEffect drunk = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("brewery", "drunk"));
                if (drunk != null) {
                    p.m_7292_(new MobEffectInstance(drunk, 200, 0, false, false, true));
                }
                p.m_7292_(new MobEffectInstance(MobEffects.f_19604_, 160, 0, false, false, true));
                MobEffect tipsy = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("brewinandchewin", "tipsy"));
                if (tipsy == null) continue;
                p.m_7292_(new MobEffectInstance(tipsy, 240, 4, false, false, true));
            }
        }
    }

    private void launchCannonballs(LivingEntity target) {
        ServerLevel sl;
        block4: {
            block3: {
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block3;
                sl = (ServerLevel)level;
                if (target != null && target.m_6084_()) break block4;
            }
            return;
        }
        int n = 3 + this.f_19796_.m_188503_(3);
        for (int i = 0; i < n; ++i) {
            double offX = (this.f_19796_.m_188500_() - 0.5) * 7.0;
            double offZ = (this.f_19796_.m_188500_() - 0.5) * 7.0;
            double dx = target.m_20185_() + offX - this.m_20185_();
            double dz = target.m_20189_() + offZ - this.m_20189_();
            double dist = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            double dy = target.m_20188_() - (this.m_20186_() + 1.8);
            double hSpeed = 1.05;
            double flight = dist / hSpeed;
            double vy = dy / flight + 0.0225 * flight;
            double px = this.m_20185_() + dx / dist * 2.2;
            double pz = this.m_20189_() + dz / dist * 2.2;
            BarrelCannonballEntity ball = new BarrelCannonballEntity(this.m_9236_(), (LivingEntity)this);
            ball.m_6034_(px, this.m_20186_() + 1.9, pz);
            ball.m_20334_(dx / dist * hSpeed, vy, dz / dist * hSpeed);
            ManorFightMembers.inherit((Entity)this, (Entity)ball);
            sl.m_7967_((Entity)ball);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11932_, SoundSource.HOSTILE, 1.6f, 0.7f);
        this.part(this.m_20185_(), this.m_20186_() + 1.9, this.m_20189_(), 18, ParticleTypes.f_123755_, 0.5, 0.05);
        FTBossMod.LOGGER.info("[FTBoss] \u9152\u6876\u738b\u70ae\u5f39\u9f50\u5c04: {} \u9897 (\u7206\u70b8\u5a01\u529b = \u95ea\u7535\u82e6\u529b\u6015 {})", (Object)n, (Object)Float.valueOf(3.0f));
    }

    private void shootFireball(LivingEntity target) {
        if (target == null || !target.m_6084_()) {
            return;
        }
        try {
            MagicFireball f = new MagicFireball(this.m_9236_(), (LivingEntity)this);
            double dx = target.m_20185_() - this.m_20185_();
            double dy = target.m_20188_() - (this.m_20186_() + 1.6);
            double dz = target.m_20189_() - this.m_20189_();
            Vec3 dir = new Vec3(dx, dy, dz).m_82541_();
            f.m_6034_(this.m_20185_() + dir.f_82479_ * 1.8, this.m_20186_() + 1.6, this.m_20189_() + dir.f_82481_ * 1.8);
            f.setDamage(11.0f + (float)BossRage.tierOf((LivingEntity)this) * 4.0f);
            ManorFightMembers.inherit((Entity)this, (Entity)f);
            this.m_9236_().m_7967_((Entity)f);
            f.shoot(dir);
            this.m_9236_().m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11705_, SoundSource.HOSTILE, 1.2f, 0.85f);
            this.part(f.m_20185_(), f.m_20186_(), f.m_20189_(), 10, ParticleTypes.f_123744_, 0.2, 0.02);
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u9152\u6876\u738b\u706b\u7130\u5f39\u5f02\u5e38: {}", (Object)t.toString());
        }
    }

    private void fillSpellQueue(LivingEntity target) {
        ArrayList<AbstractSpell> pool = new ArrayList<AbstractSpell>();
        for (AbstractSpell s : WizardSummon.spellPool()) {
            if (!WizardSummon.isFire(s)) continue;
            pool.add(s);
        }
        if (pool.isEmpty()) {
            return;
        }
        int rage = BossRage.tierOf((LivingEntity)this);
        if (rage >= 1 && this.f_19796_.m_188501_() < 0.7f) {
            ArrayList<AbstractSpell> rare = new ArrayList<AbstractSpell>();
            for (AbstractSpell s : pool) {
                if (WizardSummon.rarityOf(s, s.getMaxLevel()) < 3) continue;
                rare.add(s);
            }
            if (!rare.isEmpty()) {
                pool = rare;
            }
        }
        int n = 3 + this.f_19796_.m_188503_(3);
        for (int i = 0; i < n && !pool.isEmpty(); ++i) {
            this.spellQueue.add((AbstractSpell)pool.remove(this.f_19796_.m_188503_(pool.size())));
        }
    }

    private void castNextSpell(LivingEntity target) {
        if (this.spellQueue.isEmpty() || target == null || !target.m_6084_()) {
            this.spellQueue.clear();
            return;
        }
        AbstractSpell s = this.spellQueue.remove(0);
        if (!BossSpellPacing.tryLegacyCast((LivingEntity)this, s)) {
            return;
        }
        int lvl = Math.max(s.getMinLevel(), Math.min(4 + BossRage.tierOf((LivingEntity)this), s.getMaxLevel()));
        try {
            try {
                Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                Object inst = tdc.getConstructor(Player.class).newInstance(target);
                Class<?> mdCls = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.MagicData");
                Object md = mdCls.getMethod("getPlayerMagicData", LivingEntity.class).invoke(null, this);
                mdCls.getMethod("setAdditionalCastData", Class.forName("io.redspace.ironsspellbooks.api.spells.CastData")).invoke(md, inst);
            }
            catch (Throwable tdc) {
                // empty catch block
            }
            this.initiateCastSpell(s, lvl);
            FTBossMod.LOGGER.info("[FTBoss] \u9152\u9b54\u65bd\u6cd5: {} lv{} \u76ee\u6807={}", new Object[]{s.getSpellId(), lvl, target.m_7755_().getString()});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u9152\u9b54\u65bd\u6cd5\u5f02\u5e38: {}", (Object)t.toString());
        }
    }

    private void summonMinions(LivingEntity target) {
        try {
            this.ensureWineEffects();
            for (int i = 0; i < 2 + this.f_19796_.m_188503_(2); ++i) {
                SmallBarrelEntity minion = (SmallBarrelEntity)((EntityType)FTEntities.BARREL_MINION.get()).m_20615_(this.m_9236_());
                if (minion == null) continue;
                this.placeAndBoost((Mob)minion, target, "\u00a76\u5c0f\u9152\u6876");
            }
            try {
                EntityType e;
                Entity raw;
                EntityType beer = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("brewery", "beer_elemental"));
                if (beer != null && (raw = (e = beer).m_20615_(this.m_9236_())) instanceof Mob) {
                    Mob beerMob = (Mob)raw;
                    this.placeAndBoost(beerMob, target, "\u00a75\u5564\u9152\u4eba");
                }
            }
            catch (Throwable beer) {}
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u9152\u9b54\u53ec\u5524\u5f02\u5e38: {}", (Object)t.toString());
        }
    }

    private void placeAndBoost(Mob m, LivingEntity target, String name) {
        double a = this.f_19796_.m_188500_() * Math.PI * 2.0;
        double r = 2.5 + this.f_19796_.m_188500_() * 3.5;
        m.m_6034_(this.m_20185_() + Math.cos(a) * r, this.m_20186_() + 0.5, this.m_20189_() + Math.sin(a) * r);
        m.m_6593_((Component)Component.m_237113_((String)name));
        m.m_20340_(true);
        m.m_146915_(true);
        if (target != null) {
            m.m_6710_(target);
        }
        this.ensureWineEffects();
        for (MobEffect fx : new MobEffect[]{this.wineArmor, this.wineHealth, this.wineResist, this.wineLuck}) {
            MobEffectInstance cur;
            if (fx == null || (cur = this.m_21124_(fx)) == null) continue;
            m.m_7292_(new MobEffectInstance(fx, cur.m_19557_(), cur.m_19564_(), false, false, true));
        }
        ManorFightMembers.inherit((Entity)this, (Entity)m);
        this.m_9236_().m_7967_((Entity)m);
        this.minions.add(m.m_20148_());
    }

    private ServerLevel sl() {
        ServerLevel s;
        Level level = this.m_9236_();
        return level instanceof ServerLevel ? (s = (ServerLevel)level) : null;
    }

    private void fx(double x, double y, double z, int n, float r, float g, float b, double spread, double speed) {
        Fx.dust(this.m_9236_(), x, y, z, n, r, g, b, spread, speed);
    }

    private void part(double x, double y, double z, int n, SimpleParticleType type, double spread, double speed) {
        Fx.particles(this.m_9236_(), x, y, z, n, (ParticleOptions)type, spread, speed);
    }

    private void snd(SoundEvent e, float vol, float pitch) {
        Fx.sound(this.m_9236_(), this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), e, vol, pitch);
    }

    private void tickBarrelActions() {
        if (this.m_21224_()) {
            return;
        }
        if (this.bState != 0) {
            float yaw = (float)Math.toDegrees(Math.atan2(-this.chargeDx, this.chargeDz));
            this.m_146922_(yaw);
            this.m_5618_(yaw);
            this.m_5616_(yaw);
        }
        if (--this.mistCd <= 0) {
            this.mistCd = 10;
            this.tactics.mist();
        }
        switch (this.bState) {
            case 0: {
                LivingEntity t = this.tactics.nearestPlayer(22.0);
                if (this.chargeCd-- <= 0 && t != null) {
                    this.lockFacing(t);
                    this.bState = 1;
                    this.bTimer = 10;
                    this.snd(SoundEvents.f_12638_, 1.1f, 0.55f);
                    this.part(this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 8, ParticleTypes.f_123759_, 0.7, 0.03);
                    break;
                }
                if (this.popCd-- > 0 || t == null) break;
                this.lockFacing(t);
                this.pendingVolley = "";
                this.bState = 4;
                this.bTimer = 50;
                break;
            }
            case 1: {
                if (this.bTimer % 2 == 0) {
                    this.tactics.warnCharge(this.chargeDx, this.chargeDz);
                }
                if (--this.bTimer > 0) break;
                this.bState = 2;
                this.bTimer = 22;
                break;
            }
            case 2: {
                this.m_20334_(this.chargeDx * 0.42, this.m_20184_().f_82480_, this.chargeDz * 0.42);
                this.f_19812_ = true;
                if (this.f_19797_ % 3 == 0) {
                    this.tactics.rollTrail();
                }
                if (this.f_19797_ % 6 == 0) {
                    this.snd(SoundEvents.f_12638_, 1.5f, 0.45f);
                }
                if (this.f_19797_ % 8 == 0) {
                    this.tactics.hitPlayers(2.9, 8.0f, 1.1);
                }
                if (--this.bTimer > 0 && !this.f_19862_) break;
                this.bState = 3;
                this.bTimer = 24;
                this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
                break;
            }
            case 3: {
                if (this.bTimer > 14 && this.bTimer % 2 == 0) {
                    this.tactics.warnSlam();
                }
                if (this.bTimer == 14) {
                    this.tactics.slam();
                }
                if (--this.bTimer > 0) break;
                this.bState = 0;
                this.chargeCd = 130;
                break;
            }
            case 4: {
                if (this.bTimer == 40) {
                    this.releaseVolley();
                }
                if (this.pendingVolley.isEmpty() && this.bTimer <= 40 && this.bTimer >= 10 && this.bTimer % 4 == 0) {
                    this.tactics.sprayTrail();
                }
                if (--this.bTimer > 0) break;
                this.bState = 0;
                this.popCd = 100;
                break;
            }
            default: {
                this.bState = 0;
            }
        }
    }

    private void lockFacing(LivingEntity target) {
        Vec3 direction = target.m_20182_().m_82546_(this.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        this.chargeDx = direction.f_82479_;
        this.chargeDz = direction.f_82481_;
        float yaw = (float)Math.toDegrees(Math.atan2(-direction.f_82479_, direction.f_82481_));
        this.m_146922_(yaw);
        this.m_5618_(yaw);
        this.m_5616_(yaw);
    }

    private void startVolley(String kind, LivingEntity target) {
        this.lockFacing(target);
        this.pendingVolley = kind;
        this.volleyTarget = target.m_20148_();
        this.bState = 4;
        this.bTimer = 50;
    }

    private void releaseVolley() {
        LivingEntity target;
        if (this.pendingVolley.isEmpty()) {
            this.tactics.corkSpray();
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        Object object = server.m_8791_(this.volleyTarget);
        if (!(object instanceof LivingEntity) || !BossTargets.valid(target = (LivingEntity)object) || this.m_20280_((Entity)target) > 1024.0) {
            return;
        }
        switch (this.pendingVolley) {
            case "cannon": {
                this.launchCannonballs(target);
                break;
            }
            case "fireball": {
                this.shootFireball(target);
                break;
            }
            case "summon": {
                this.summonMinions(target);
                break;
            }
        }
    }

    private void tickMusic() {
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

    public void m_142687_(Entity.RemovalReason reason) {
        BossBars.untrack((Entity)this, this.bossBar);
        this.stopMusic();
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            for (UUID u : this.minions) {
                Entity e = sl.m_8791_(u);
                if (e == null) continue;
                e.m_146870_();
            }
            SummonSweep.clear((LivingEntity)this, 80.0, WineBarrelDecoyEntity.class, SmallBarrelEntity.class);
        }
        this.minions.clear();
        super.m_142687_(reason);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    protected boolean m_21527_() {
        return false;
    }
}

