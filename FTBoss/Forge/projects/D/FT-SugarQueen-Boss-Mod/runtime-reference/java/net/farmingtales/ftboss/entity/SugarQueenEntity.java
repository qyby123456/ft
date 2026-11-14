/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.entity.IMagicEntity
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob
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
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
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

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import java.util.ArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossMeleeWindup;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossUnstuck;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossCollision;
import net.farmingtales.ftboss.util.BossSpellRules;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class SugarQueenEntity
extends AbstractSpellCastingMob
implements RegularBossCastHost {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(SugarQueenEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(SugarQueenEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, DATA_SIGNATURE_CAST);
    private final BossMeleeWindup meleeWindup = new BossMeleeWindup((Mob)this, this.actionState, 5, 14, 3.2);
    private static final ResourceLocation SWEETS_ID = new ResourceLocation("farm_and_charm", "sweets");
    private static final ResourceLocation SWEET_HEART_ID = new ResourceLocation("brewinandchewin", "sweet_heart");
    private static MobEffect cachedSweets;
    private static MobEffect cachedSweetHeart;
    private static boolean effectsWarned;
    private boolean auraLogged = false;
    private boolean musicStarted = false;
    private int auraCounter = 0;
    private boolean cloneQueen = false;
    private ServerBossEvent cloneBar;
    private ServerBossEvent bossBar;
    private static final double BAR_RANGE = 48.0;
    private int cloneCastCd = 40;
    private int cloneMeleeCd = 0;
    private static final RawAnimation ANIM_IDLE;
    private static final RawAnimation ANIM_WALK;
    private static final RawAnimation ANIM_CAST;
    private static final RawAnimation ANIM_SIGNATURE_CAST;
    private static final RawAnimation ANIM_SIGNATURE_SELF;
    private static final RawAnimation ANIM_HURT;
    private static final RawAnimation ANIM_SLAM;
    private static final RawAnimation ANIM_DEATH;

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, (Object)new CompoundTag());
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(RegularBossCastHost.action(kind), RegularBossCastHost.visualTicks(castTicks));
    }

    public boolean m_7327_(Entity target) {
        this.meleeWindup.start(target);
        return false;
    }

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

    public void setCloneQueen(boolean v) {
        this.cloneQueen = v;
    }

    public boolean isCloneQueen() {
        return this.cloneQueen;
    }

    public SugarQueenEntity(EntityType<? extends SugarQueenEntity> type, Level level) {
        super(type, level);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "gingerbread_ctrl", 6, state -> {
            this.actionState.prepare(state);
            this.signatureCast.prepare(state);
            if (this.m_21224_()) {
                state.setAnimation(ANIM_DEATH);
            } else if (this.signatureCast.active()) {
                state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? ANIM_SIGNATURE_SELF : ANIM_SIGNATURE_CAST);
            } else if ("melee".equals(this.actionState.name())) {
                state.setAnimation(ANIM_SLAM);
            } else if (this.isCasting()) {
                if (!state.isCurrentAnimation(ANIM_CAST)) {
                    state.setAnimation(ANIM_CAST);
                }
            } else if (this.f_20916_ > 2) {
                if (!state.isCurrentAnimation(ANIM_HURT)) {
                    state.setAnimation(ANIM_HURT);
                }
            } else if (this.m_20184_().m_165925_() > 8.0E-4) {
                if (!state.isCurrentAnimation(ANIM_WALK)) {
                    state.setAnimation(ANIM_WALK);
                }
            } else if (!state.isCurrentAnimation(ANIM_IDLE)) {
                state.setAnimation(ANIM_IDLE);
            }
            return PlayState.CONTINUE;
        })});
    }

    public boolean m_6469_(DamageSource source, float amount) {
        float gated = BossDamageGate.gate((LivingEntity)this, source, amount);
        if (gated <= 0.0f) {
            return false;
        }
        boolean hit = super.m_6469_(source, gated);
        if (hit) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        return hit;
    }

    public void m_8119_() {
        super.m_8119_();
        if (!this.m_9236_().f_46443_) {
            this.meleeWindup.tick(x$0 -> super.m_7327_(x$0));
            BossDamageGate.tick((LivingEntity)this);
            BossRage.tickBoss((LivingEntity)this);
            TrapBreaker.tick((LivingEntity)this);
            BossUnstuck.tick((LivingEntity)this);
            MinionBlessing.tick((LivingEntity)this, 40.0);
            this.tickSweetAura();
            this.tickPurgeDebuffs();
            this.tickMusic();
            if (this.cloneQueen) {
                this.tickCloneBrain();
            } else {
                this.tickBossBar();
            }
        }
    }

    private void tickBossBar() {
        if (this.bossBar == null) {
            this.bossBar = new ServerBossEvent((Component)Component.m_237115_((String)"boss.ftboss.sugar_queen"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
            BossBarVisibility.visible(this.bossBar, true);
            BossBars.track((Entity)this, this.bossBar);
        }
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.bossBar);
            this.bossBar = null;
            return;
        }
        this.bossBar.m_142711_(Math.max(0.0f, Math.min(1.0f, this.m_21223_() / Math.max(1.0f, this.m_21233_()))));
        boolean want = false;
        for (ServerPlayer p : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (p.m_6084_() && this.m_20280_((Entity)p) < 2304.0) {
                BossBarVisibility.addPlayer(this.bossBar, p);
                want = true;
                continue;
            }
            this.bossBar.m_6539_(p);
        }
        if (this.bossBar.m_8323_() != want) {
            BossBarVisibility.visible(this.bossBar, want);
        }
    }

    private void tickCloneBrain() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (this.cloneBar == null) {
            this.cloneBar = new ServerBossEvent(this.m_5446_(), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
            BossBars.track((Entity)this, this.cloneBar);
            BossBarVisibility.visible(this.cloneBar, true);
        }
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.cloneBar);
            return;
        }
        this.cloneBar.m_142711_(this.m_21223_() / this.m_21233_());
        LivingEntity target = BossTargets.combatTarget((Mob)this, 32.0);
        if (target == null) {
            return;
        }
        this.m_6710_(target);
        double dx = target.m_20185_() - this.m_20185_();
        double dy = target.m_20186_() + 2.2 - this.m_20186_();
        double dz = target.m_20189_() - this.m_20189_();
        double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dl > 2.0) {
            double step = Math.min(0.25, dl);
            BossCollision.move((Entity)this, new Vec3(dx / dl * step, dy / dl * step, dz / dl * step));
        }
        this.m_21563_().m_24960_((Entity)target, 30.0f, 30.0f);
        if (--this.cloneCastCd <= 0 && !this.isCasting()) {
            this.cloneCastCd = NonManorFinaleDifficulty.acceleratedTicks((LivingEntity)this, 70 + this.m_217043_().m_188503_(40));
            WizardSummon.castRandom((IMagicEntity)this, target, this.m_217043_(), 3 + BossRage.tierOf((LivingEntity)this));
        }
        --this.cloneMeleeCd;
        if (this.cloneMeleeCd <= 0 && (double)this.m_20270_((Entity)target) < 3.0) {
            this.cloneMeleeCd = 30;
            this.m_7327_((Entity)target);
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
        this.stopMusic();
        super.m_142687_(reason);
    }

    private void tickSweetAura() {
        if (++this.auraCounter < 10) {
            return;
        }
        this.auraCounter = 0;
        if (cachedSweets == null) {
            cachedSweets = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(SWEETS_ID);
        }
        if (cachedSweetHeart == null) {
            cachedSweetHeart = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(SWEET_HEART_ID);
        }
        if (cachedSweets == null || cachedSweetHeart == null) {
            if (!effectsWarned) {
                effectsWarned = true;
                FTBossMod.LOGGER.warn("[FTBoss] \u627e\u4e0d\u5230\u5916\u6765\u6548\u679c(\u7f3a mod?): sweets={}, sweet_heart={}", (Object)cachedSweets, (Object)cachedSweetHeart);
            }
            return;
        }
        effectsWarned = false;
        this.keepEffect(cachedSweets, 2);
        this.keepEffect(cachedSweetHeart, 0);
        if (!this.auraLogged) {
            this.auraLogged = true;
            FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687\u5df2\u83b7\u5f97\u5e38\u9a7b\u6548\u679c: \u751c\u871c(farm_and_charm:sweets) + \u751c\u871c\u4e4b\u5fc3(brewinandchewin:sweet_heart), \u8d1f\u9762\u6548\u679c\u514d\u75ab\u5f00\u542f");
        }
    }

    private void keepEffect(MobEffect fx, int amp) {
        MobEffectInstance cur = this.m_21124_(fx);
        if (cur == null || cur.m_19557_() < 120) {
            this.m_7292_(new MobEffectInstance(fx, 600, amp, false, false, true));
        }
    }

    private void tickPurgeDebuffs() {
        if (this.f_19797_ % 20 != 0) {
            return;
        }
        for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
            if (inst.m_19544_().m_19486_()) continue;
            this.m_21195_(inst.m_19544_());
        }
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 360.0).m_22268_(Attributes.f_22284_, 2.5).m_22268_(Attributes.f_22281_, 1.5).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 40.0);
    }

    static {
        effectsWarned = false;
        ANIM_IDLE = RawAnimation.begin().thenLoop("animation.king.idle");
        ANIM_WALK = RawAnimation.begin().thenLoop("animation.king.walk");
        ANIM_CAST = RawAnimation.begin().thenPlay("animation.king.scepter_raise").thenLoop("long_cast");
        ANIM_SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.king.signature_cast");
        ANIM_SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.king.signature_self_cast");
        ANIM_HURT = RawAnimation.begin().thenPlay("animation.king.hurt");
        ANIM_SLAM = RawAnimation.begin().thenPlay("animation.king.scepter_slam");
        ANIM_DEATH = RawAnimation.begin().thenPlay("animation.king.death");
    }
}

