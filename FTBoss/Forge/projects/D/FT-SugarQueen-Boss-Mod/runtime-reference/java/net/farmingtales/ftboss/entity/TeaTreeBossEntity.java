/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob
 *  net.minecraft.core.BlockPos
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
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.projectile.EvokerFangs
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossMeleeWindup;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.boss.ai.SayaBuds;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.boss.ai.TeaTactics;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.saya.SayaCastSync;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class TeaTreeBossEntity
extends AbstractSpellCastingMob
implements RegularBossCastHost {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(TeaTreeBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(TeaTreeBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, DATA_SIGNATURE_CAST);
    private final BossMeleeWindup meleeWindup = new BossMeleeWindup((Mob)this, this.actionState, 6, 14, 3.0);
    private UUID nativeTarget;
    private Vec3 nativeAim = Vec3.f_82478_;
    private Vec3 rootDirection = Vec3.f_82478_;
    private boolean nativePending;
    private static final double BOSS_BAR_RANGE = 48.0;
    private static final double MAX_HP = 340.0;
    private static final String FINALE_TAG = "ftboss:saya_finale";
    private static final double FINALE_HEALTH_MULTIPLIER = 2.0;
    private static final double FINALE_ATTACK_MULTIPLIER = 2.0;
    private static final int FANG_CD = 100;
    private static final int SPIT_CD = 160;
    private static final int FANG_CD_P2 = 62;
    private static final int SPIT_CD_P2 = 100;
    private static final int HP_BOOST_INTERVAL = 1200;
    private static final int HP_BOOST_MAX = 10;
    private static final float HP_BOOST_PER_LEVEL = 4.0f;
    private final ServerBossEvent bossBar;
    private MobEffect teaRenewal;
    private int auraTimer = 0;
    private int fangCd = 100;
    private int spitCd = 160;
    private int contactCd = 0;
    private int castCd = 80;
    private int buffWheelCd = 60;
    private static final String MINION_TAG = "tea_minion";
    private final SayaBuds buds = new SayaBuds(this);
    private boolean musicStarted = false;
    private final TeaTactics tactics = new TeaTactics(this);
    private final List<AbstractSpell> castQueue = new ArrayList<AbstractSpell>();
    private boolean phase2 = false;
    private boolean secondLifeUsed = false;
    private int hpBoostLevel = 0;
    private static final String[] SAYA_SELF_SPELL_FIELDS = new String[]{"OAKSKIN_SPELL", "ABSORPTION_SPELL"};
    private List<AbstractSpell> sayaSelfSpells;

    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (this.isCasting()) {
            return;
        }
        super.initiateCastSpell(spell, spellLevel);
        BossSpellTuning.fasterLegacyCast(this, spell);
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, (Object)new CompoundTag());
    }

    public boolean m_7327_(Entity target) {
        this.meleeWindup.start(target);
        return false;
    }

    public BossActionState nativeAction() {
        return this.actionState;
    }

    public BossActionState signatureCastAction() {
        return this.signatureCast;
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int n) {
        this.signatureCast.start(SayaCastSync.action(kind, n), SayaCastSync.duration(n));
    }

    private void startNativeSkill(String name, LivingEntity target) {
        this.nativeTarget = target.m_20148_();
        this.nativeAim = target.m_146892_();
        this.rootDirection = target.m_20182_().m_82546_(this.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        this.nativePending = true;
        this.actionState.start(name, "roots".equals(name) ? 26 : 20);
    }

    private void tickNativeSkill() {
        LivingEntity target;
        int release;
        Level level;
        this.meleeWindup.tick(x$0 -> super.m_7327_(x$0));
        if (!this.nativePending || !((level = this.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        if (!this.actionState.active()) {
            this.nativePending = false;
            return;
        }
        boolean roots = "roots".equals(this.actionState.name());
        int n = release = roots ? 12 : 8;
        if (this.actionState.elapsed(0.0f) < (float)release) {
            if (this.f_19797_ % 3 == 0) {
                if (roots) {
                    for (int step = 2; step <= 16; step += 2) {
                        double pointX = this.m_20185_() + this.rootDirection.f_82479_ * (double)step;
                        double pointZ = this.m_20189_() + this.rootDirection.f_82481_ * (double)step;
                        double ground = server.m_6924_(Heightmap.Types.MOTION_BLOCKING, Mth.m_14107_((double)pointX), Mth.m_14107_((double)pointZ));
                        server.m_8767_((ParticleOptions)ParticleTypes.f_123749_, pointX, ground + 0.1, pointZ, 1, 0.06, 0.02, 0.06, 0.0);
                    }
                } else {
                    server.m_8767_((ParticleOptions)ParticleTypes.f_123763_, this.m_20185_(), this.m_20186_() + 1.4, this.m_20189_(), 3, 0.2, 0.12, 0.2, 0.01);
                }
            }
            return;
        }
        this.nativePending = false;
        Entity pointX = server.m_8791_(this.nativeTarget);
        if (!(pointX instanceof LivingEntity) || !BossTargets.valid(target = (LivingEntity)pointX)) {
            return;
        }
        if (roots) {
            this.eruptionRoots(target);
        } else {
            Vec3 origin = this.m_146892_();
            Vec3 delta = this.nativeAim.m_82546_(origin);
            for (int step = 0; step <= 16; ++step) {
                Vec3 point = origin.m_82549_(delta.m_82490_((double)step / 16.0));
                server.m_8767_((ParticleOptions)ParticleTypes.f_123763_, point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.07, 0.07, 0.07, 0.01);
            }
            if (target.m_20191_().m_82400_(0.85).m_82390_(this.nativeAim) && this.m_142582_((Entity)target) && this.m_20280_((Entity)target) <= 256.0) {
                this.spitTeaPoison(target);
            }
        }
    }

    public boolean m_6051_() {
        return false;
    }

    public boolean isTeaMinion() {
        return this.getPersistentData().m_128471_(MINION_TAG);
    }

    public void setTeaMinion(boolean v) {
        this.getPersistentData().m_128379_(MINION_TAG, v);
    }

    public boolean isFinaleChallenge() {
        return this.getPersistentData().m_128471_(FINALE_TAG);
    }

    public void setFinaleChallenge(boolean finale) {
        this.getPersistentData().m_128379_(FINALE_TAG, finale);
        if (this.m_9236_().f_46443_) {
            return;
        }
        AttributeInstance health = this.m_21051_(Attributes.f_22276_);
        AttributeInstance attack = this.m_21051_(Attributes.f_22281_);
        if (health != null) {
            health.m_22100_(340.0 * (finale ? 2.0 : 1.0));
        }
        if (attack != null) {
            attack.m_22100_(3.5 * (finale ? 2.0 : 1.0));
        }
        this.m_21153_(this.m_21233_());
        this.updateBossBarName();
    }

    private void updateBossBarName() {
        this.bossBar.m_6456_((Component)Component.m_237115_((String)"boss.ftboss.saya").m_7220_((Component)(this.isFinaleChallenge() ? Component.m_237113_((String)" \u00a74[\u7ec8\u7ed3]") : Component.m_237119_())));
    }

    public TeaTreeBossEntity(EntityType<? extends TeaTreeBossEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 150;
        this.bossBar = new ServerBossEvent((Component)Component.m_237115_((String)"boss.ftboss.saya"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 340.0).m_22268_(Attributes.f_22284_, 5.0).m_22268_(Attributes.f_22281_, 3.5).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 40.0);
    }

    protected void m_8099_() {
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if (!this.isTeaMinion() && this.buds.shielded()) {
            this.buds.onBlocked(source);
            return false;
        }
        if ((amount = BossDamageGate.gate((LivingEntity)this, source, amount)) <= 0.0f) {
            return false;
        }
        boolean hit = super.m_6469_(source, amount);
        if (hit) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        if (hit) {
            BossBarVisibility.visible(this.bossBar, true);
        }
        return hit;
    }

    public void m_8119_() {
        super.m_8119_();
        if (!this.m_9236_().f_46443_) {
            BossDamageGate.tick((LivingEntity)this);
            BossRage.tickBoss((LivingEntity)this);
            this.tickServer();
            if (!this.isTeaMinion()) {
                TrapBreaker.tick((LivingEntity)this);
                this.buds.tick();
                this.tactics.tickMinionSummon();
                MinionBlessing.tick((LivingEntity)this, 40.0);
            }
        }
        this.tickBossBar();
    }

    private void tickMusic(LivingEntity target) {
        if (!this.m_6084_()) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        this.musicStarted = true;
        BossMusic.request(sl, (LivingEntity)this);
    }

    private void stopMusic() {
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

    private void tickServer() {
        int sc;
        if (!this.m_6084_()) {
            this.nativePending = false;
            return;
        }
        this.tickNativeSkill();
        if (this.f_19797_ % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
                if (inst.m_19544_().m_19486_()) continue;
                this.m_21195_(inst.m_19544_());
            }
        }
        this.tickTeaAura();
        this.tickBuffWheel();
        this.tickLifeGrowth();
        this.tactics.absorb();
        LivingEntity target = this.nearestTarget(20.0);
        if (target == null) {
            return;
        }
        this.m_6710_(target);
        this.faceTarget(target);
        this.tickMusic(target);
        if (!this.phase2 && (double)(this.m_21223_() / this.m_21233_()) < 0.5) {
            this.phase2 = true;
            FTBossMod.LOGGER.info("[FTBoss] \u8336\u6811\u53e4\u6811 \u8336\u5fc3\u89c9\u9192: \u6839\u523a\u4e0e\u8336\u6bd2\u63d0\u901f");
            Level level = this.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 1.0f, 0.6f);
            }
            this.buds.trigger();
        }
        int fc = this.phase2 ? 62 : 100;
        int n = sc = this.phase2 ? 100 : 160;
        if (this.isFinaleChallenge()) {
            fc = (fc + 1) / 2;
            sc = (sc + 1) / 2;
        }
        double dist = this.m_20270_((Entity)target);
        this.tickIronMagic(target);
        if (this.actionState.active()) {
            return;
        }
        --this.fangCd;
        if (this.fangCd <= 0) {
            this.fangCd = fc;
            if (dist < 16.0 && dist > 1.5) {
                this.startNativeSkill("roots", target);
                return;
            }
        }
        --this.spitCd;
        if (this.spitCd <= 0) {
            this.spitCd = sc;
            if (dist < 16.0) {
                this.startNativeSkill("poison", target);
                return;
            }
        }
        --this.contactCd;
        if (this.contactCd <= 0 && dist < 3.0) {
            this.contactCd = this.isFinaleChallenge() ? 15 : 30;
            this.m_7327_((Entity)target);
        }
    }

    private void tickIronMagic(LivingEntity target) {
        if (this.isCasting()) {
            return;
        }
        --this.castCd;
        if (this.castCd <= 0) {
            int nextCast = this.phase2 ? 44 : 62;
            int n = this.castCd = this.isFinaleChallenge() ? (nextCast + 1) / 2 : nextCast;
            if (this.castQueue.isEmpty()) {
                this.fillCastQueue();
            }
            if (!this.castQueue.isEmpty()) {
                this.tryCastSpell(target, this.castQueue.remove(0));
            }
        }
    }

    private void fillCastQueue() {
        List<AbstractSpell> pool = WizardSummon.spellPool();
        if (pool.isEmpty()) {
            return;
        }
        if (BossRage.tierOf((LivingEntity)this) >= 1 && this.f_19796_.m_188501_() < 0.8f) {
            pool = WizardSummon.spellPoolRare(8, 3);
        }
        int n = 2;
        ArrayList<AbstractSpell> tmp = new ArrayList<AbstractSpell>(pool);
        while (!tmp.isEmpty() && this.castQueue.size() < n) {
            this.castQueue.add((AbstractSpell)tmp.remove(this.f_19796_.m_188503_(tmp.size())));
        }
        if (this.f_19796_.m_188501_() < 0.35f) {
            if (this.sayaSelfSpells == null) {
                this.sayaSelfSpells = WizardSummon.spellPoolOf(SAYA_SELF_SPELL_FIELDS);
            }
            if (!this.sayaSelfSpells.isEmpty()) {
                this.castQueue.add(this.sayaSelfSpells.get(this.f_19796_.m_188503_(this.sayaSelfSpells.size())));
            }
        }
    }

    private void tryCastSpell(LivingEntity target, AbstractSpell s) {
        try {
            if (s == null) {
                return;
            }
            if (!BossSpellPacing.tryLegacyCast((LivingEntity)this, s)) {
                return;
            }
            int lvl = Math.max(s.getMinLevel(), Math.min(6 + BossRage.tierOf((LivingEntity)this), s.getMaxLevel()));
            try {
                Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                for (Constructor<?> ctor : tdc.getConstructors()) {
                    if (ctor.getParameterCount() != 1 || !ctor.getParameterTypes()[0].isInstance(target)) continue;
                    Object inst = ctor.newInstance(target);
                    Method set = MagicData.class.getMethod("setAdditionalCastData", ICastData.class);
                    MagicData md = MagicData.getPlayerMagicData((LivingEntity)this);
                    set.invoke(md, inst);
                    break;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.initiateCastSpell(s, lvl);
            FTBossMod.LOGGER.info("[FTBoss] \u8336\u6811\u53e4\u6811\u65bd\u6cd5: {} lv{} \u76ee\u6807={}", new Object[]{s.getSpellId(), lvl, target.m_7755_().getString()});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u8336\u6811\u53e4\u6811\u65bd\u6cd5\u5f02\u5e38: {}", (Object)t.toString());
        }
    }

    private void eruptionRoots(LivingEntity target) {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        Vec3 dir = this.rootDirection;
        double baseX = this.m_20185_();
        double baseZ = this.m_20189_();
        double step = 1.6;
        int count = 9 + this.f_19796_.m_188503_(3);
        for (int i = 1; i <= count; ++i) {
            double px = baseX + dir.f_82479_ * (double)i * step;
            double pz = baseZ + dir.f_82481_ * (double)i * step;
            BlockPos pos = BlockPos.m_274561_((double)px, (double)this.m_20186_(), (double)pz);
            double py = (double)this.m_9236_().m_6924_(Heightmap.Types.MOTION_BLOCKING, pos.m_123341_(), pos.m_123343_()) + 0.0;
            EvokerFangs fangs = new EvokerFangs(this.m_9236_(), px, py, pz, (float)Math.atan2(dir.f_82481_, dir.f_82479_), (i - 1) * 2, (LivingEntity)this);
            ManorFightMembers.inherit((Entity)this, (Entity)fangs);
            sl.m_7967_((Entity)fangs);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_144209_, SoundSource.HOSTILE, 1.0f, 0.7f);
    }

    private void spitTeaPoison(LivingEntity target) {
        target.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 220, 2, false, false, true));
        target.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), this.isFinaleChallenge() ? 5.0f : 2.5f);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_6263_(null, target.m_20185_(), target.m_20186_(), target.m_20189_(), SoundEvents.f_144088_, SoundSource.HOSTILE, 1.0f, 1.3f);
        }
    }

    private void tickBuffWheel() {
        if (--this.buffWheelCd > 0) {
            return;
        }
        this.buffWheelCd = 110 + this.f_19796_.m_188503_(60);
        List<MobEffect> pool = List.of(MobEffects.f_19596_, MobEffects.f_19600_, MobEffects.f_19598_, MobEffects.f_19606_, MobEffects.f_19605_, MobEffects.f_19617_, MobEffects.f_19618_, MobEffects.f_19607_, MobEffects.f_19621_, MobEffects.f_19603_, MobEffects.f_19608_);
        for (int i = 0; i < 2; ++i) {
            MobEffect fx = pool.get(this.f_19796_.m_188503_(pool.size()));
            int dur = 100 + this.f_19796_.m_188503_(120);
            int amp = this.f_19796_.m_188503_(3);
            this.m_7292_(new MobEffectInstance(fx, dur, amp, false, false, true));
        }
    }

    private void tickLifeGrowth() {
        if (this.hpBoostLevel >= 10) {
            return;
        }
        if (this.f_19797_ <= 0 || this.f_19797_ % 1200 != 0) {
            return;
        }
        if (this.nearestTarget(48.0) == null) {
            return;
        }
        ++this.hpBoostLevel;
        this.m_7292_(new MobEffectInstance(MobEffects.f_19616_, 12000, this.hpBoostLevel - 1, false, false, true));
        this.m_5634_(4.0f);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.0f, 0.8f);
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.5, this.m_20189_(), 30, (ParticleOptions)ParticleTypes.f_123748_, 1.6, 0.08);
            for (ServerPlayer p : sl.m_6907_()) {
                if (!(p.m_20280_((Entity)this) <= 2304.0)) continue;
                Component.m_237113_((String)("\u00a72\u6c99\u8036 \u00a77\u6c72\u53d6\u8336\u5fc3: \u00a7c\u751f\u547d\u63d0\u5347 " + this.hpBoostLevel + " \u7ea7 \u00a78(\u6700\u5927\u751f\u547d +" + (int)(4.0f * (float)this.hpBoostLevel) + ")"));
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] \u8336\u6811\u53e4\u6811 \u751f\u547d\u63d0\u5347 -> {} \u7ea7 (+{} \u6700\u5927\u751f\u547d)", (Object)this.hpBoostLevel, (Object)((int)(4.0f * (float)this.hpBoostLevel)));
    }

    private void tickTeaAura() {
        MobEffectInstance cur;
        if (++this.auraTimer < 10) {
            return;
        }
        this.auraTimer = 0;
        if (this.teaRenewal == null) {
            this.teaRenewal = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("herbalbrews", "renewal"));
        }
        if (this.teaRenewal != null && ((cur = this.m_21124_(this.teaRenewal)) == null || cur.m_19557_() < 120)) {
            this.m_7292_(new MobEffectInstance(this.teaRenewal, 600, 1, false, false, true));
        }
    }

    private LivingEntity nearestTarget(double range) {
        return BossTargets.combatTarget((Mob)this, range);
    }

    private void faceTarget(LivingEntity target) {
        double dz;
        double dx = target.m_20185_() - this.m_20185_();
        if (dx * dx + (dz = target.m_20189_() - this.m_20189_()) * dz < 1.0E-4) {
            return;
        }
        this.m_146922_((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        this.m_5618_(this.m_146908_());
        this.m_5616_(this.m_146908_());
    }

    public boolean tryRevive() {
        if (this.secondLifeUsed || !this.m_6084_()) {
            return false;
        }
        this.secondLifeUsed = true;
        this.m_21153_((float)((double)this.m_21233_() * 0.3));
        this.m_21219_();
        this.fangCd = 40;
        this.spitCd = 70;
        this.buds.reset();
        this.buds.trigger();
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12513_, SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        FTBossMod.LOGGER.info("[FTBoss] \u8336\u6811\u53e4\u6811 \u8336\u6839\u518d\u751f, \u8fdb\u5165\u7b2c\u4e8c\u9636\u6bb5");
        return true;
    }

    public void dropRewards() {
        boolean silverKey;
        this.buds.reset();
        SummonSweep.clear((LivingEntity)this, 80.0, TeaMinionEntity.class);
        this.stopMusic();
        if (LemonSummonRewards.suppressed((Entity)this)) {
            FTBossMod.LOGGER.info("[FTBoss][Lemon] summoned Tea Tree defeated: rewards suppressed");
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        double x = this.m_20185_();
        double y = this.m_20186_();
        double z = this.m_20189_();
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x, y, z, new ItemStack((ItemLike)Items.f_41896_, 24)), (LivingEntity)this);
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x, y + 0.4, z, new ItemStack((ItemLike)Items.f_42799_, 8)), (LivingEntity)this);
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x, y + 0.8, z, new ItemStack((ItemLike)FTBossItems.BLOOMING_THORNS.get())), (LivingEntity)this);
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x, y + 1.0, z, new ItemStack((ItemLike)FTBossItems.THORN_CROWN.get())), (LivingEntity)this);
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x + 0.25, y + 1.0, z, new ItemStack((ItemLike)FTBossItems.THORN_CHESTPLATE.get())), (LivingEntity)this);
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x - 0.25, y + 1.0, z, new ItemStack((ItemLike)FTBossItems.THORN_LEGGINGS.get())), (LivingEntity)this);
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x, y + 1.0, z + 0.25, new ItemStack((ItemLike)FTBossItems.THORN_BOOTS.get())), (LivingEntity)this);
        boolean bl = silverKey = this.isFinaleChallenge() && this.m_21232_() instanceof ServerPlayer;
        if (silverKey) {
            NonManorBossLootRouter.spawn((Level)sl, (Entity)new ItemEntity((Level)sl, x, y + 1.35, z, new ItemStack((ItemLike)FTBossItems.SILVER_KEY.get())), (LivingEntity)this);
        }
        NonManorBossLootRouter.spawn((Level)sl, (Entity)new ExperienceOrb((Level)sl, x, y, z, this.isFinaleChallenge() ? 300 : 150), (LivingEntity)this);
        FTBossMod.LOGGER.info("[FTBoss] \u6c99\u8036\u88ab\u51fb\u8d25\uff1a\u8346\u68d8\u5168\u5957\u5df2\u6389\u843d\uff0c\u7ec8\u7ed3\u96be\u5ea6\u94f6\u4e4b\u94a5={}", (Object)silverKey);
    }

    private void tickBossBar() {
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.bossBar);
            return;
        }
        if (this.isTeaMinion()) {
            this.hideBossBar();
            return;
        }
        this.updateBossBarName();
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
    }

    public void m_142687_(Entity.RemovalReason reason) {
        if (!this.m_9236_().f_46443_) {
            this.buds.reset();
            SummonSweep.clear((LivingEntity)this, 80.0, TeaMinionEntity.class);
        }
        BossBars.untrack((Entity)this, this.bossBar);
        this.stopMusic();
        super.m_142687_(reason);
    }

    public void hideBossBar() {
        BossBarVisibility.visible(this.bossBar, false);
        this.bossBar.m_7706_();
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    protected boolean m_21527_() {
        return false;
    }
}

