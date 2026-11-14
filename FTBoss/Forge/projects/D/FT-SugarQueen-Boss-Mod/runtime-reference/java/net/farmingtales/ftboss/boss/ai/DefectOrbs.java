/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.entity.IMagicEntity
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Registry
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.boss.ai;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.MaggotMinionEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.OrbLinkMath;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class DefectOrbs {
    public static final int SLOTS_BASE = 5;
    public static final int SLOTS_PER_PHASE = 3;
    public static final int SLOTS_MAX = 11;
    private static final int CYCLE = 48;
    private static final int ECHO_CYCLE = 80;
    private static final int PUNISH_CD = 40;
    private static final String[] GLASS_SPELLS = new String[]{"FIREBALL_SPELL", "SCORCH_SPELL", "BLAZE_STORM_SPELL", "MAGMA_BOMB_SPELL", "STARFALL_SPELL", "FLAMING_BARRAGE_SPELL", "FIRE_BREATH_SPELL", "FLAMING_STRIKE_SPELL", "POISON_SPLASH_SPELL", "ACID_ORB_SPELL", "ROOT_SPELL", "HEAT_SURGE_SPELL"};
    private static final String[] LIGHTNING_SPELLS = new String[]{"LIGHTNING_BOLT_SPELL", "LIGHTNING_LANCE_SPELL", "CHAIN_LIGHTNING_SPELL", "ELECTROCUTE_SPELL", "BALL_LIGHTNING_SPELL", "VOLT_STRIKE_SPELL", "THUNDERSTORM_SPELL"};
    private static final String[] FROST_SPELLS = new String[]{"ICE_BLOCK_SPELL", "FROSTWAVE_SPELL", "ICE_TOMB_SPELL", "FROSTBITE_SPELL", "RAY_OF_FROST_SPELL", "CONE_OF_COLD_SPELL", "BLIZZARD_SPELL", "ICE_SPIKES_SPELL", "SNOWBALL_SPELL", "FROST_STEP_SPELL"};
    private static final String[] ION_SPELLS = new String[]{"HASTE_SPELL", "OAKSKIN_SPELL", "BLESSING_OF_LIFE_SPELL", "FORTIFY_SPELL", "CHARGE_SPELL", "SHIELD_SPELL", "GREATER_HEAL_SPELL", "ASCENSION_SPELL", "EVASION_SPELL", "ANGEL_WINGS_SPELL", "HEAL_SPELL", "HEALING_CIRCLE_SPELL"};
    private static final String[] DARK_SPELLS = new String[]{"BLACK_HOLE_SPELL", "WITHER_SKULL_SPELL", "HEARTSTOP_SPELL", "ABYSSAL_SHROUD_SPELL", "DEVOUR_SPELL", "RAISE_DEAD_SPELL", "SCULK_TENTACLES_SPELL", "ELDRITCH_BLAST_SPELL", "SUMMON_VEX_SPELL", "SUMMON_POLAR_BEAR_SPELL"};
    private final Map<OrbType, List<AbstractSpell>> spellCache = new EnumMap<OrbType, List<AbstractSpell>>(OrbType.class);
    private static final String[] BASIC_SPELLS = new String[]{"MAGIC_MISSILE_SPELL", "FIREBOLT_SPELL", "ICICLE_SPELL", "FROSTBITE_SPELL", "MAGIC_ARROW_SPELL", "POISON_ARROW_SPELL", "SNOWBALL_SPELL"};
    private List<AbstractSpell> basicPool;
    private final DefectCoreEntity boss;
    private int orbSlots = 5;
    private int cycleCd = 48;
    private int punishCd = 0;
    private int passiveCd = 20;
    private OrbType pendingType = null;
    private int pendingTicks = 0;
    private int pendingCharge = 0;
    private final ArrayDeque<QueuedCast> castBacklog = new ArrayDeque();
    private UUID pendingOrbId;
    private AbstractSpell pendingBasicSpell;
    private int pendingBasicLevel;
    private int pendingBasicTicks;
    private LivingEntity pendingBasicTarget;
    private int beamCd = 240;
    private int beamCharge = 0;
    private int beamFire = 0;
    private int beamPulse = 10;
    private double beamDx = 0.0;
    private double beamDy = 0.0;
    private double beamDz = -1.0;
    private static final int BEAM_CHARGE = 160;
    private static final int BEAM_DURATION = 60;
    private static final int BEAM_PULSE = 10;
    private static final float BEAM_DMG = 22.0f;
    private static final double BEAM_LEN = 40.0;
    private static final double BEAM_RADIUS = 3.5;
    private int lightningUsed = 0;
    private int currentCd = 400;
    private int skillCount = 0;
    private long lastMisfire = -10000L;
    private boolean fragmentsUsed = false;
    private float orbDamageMul = 1.0f;
    private int maggotBase = 2;
    private int maggotCd = 200;
    private static final int CURRENT_MAX_ORBS = 12;
    private static final int CURRENT_EXHAUST = 100;
    private static final float CURRENT_REGEN = 0.1f;
    private int charge = 0;
    private static final int CHARGE_PER_STRIKE = 6;
    private static final int CHARGE_MAX = 60;
    private boolean passiveLogged = false;
    private int unlockedPhases = 1;
    private static final int ECHO_DELAY = 16;
    private boolean echoForm = false;
    private AbstractSpell echoSpell;
    private LivingEntity echoTarget;
    private int echoLevel;
    private int echoTicks = 0;
    private static final int[][] ORB_WEIGHTS = new int[][]{{25, 25, 20, 18, 12}, {20, 22, 20, 20, 18}, {15, 20, 18, 22, 25}};
    private static ResourceKey<DamageType> TRUE_DMG_KEY = null;
    private static boolean TRUE_DMG_TRIED = false;

    private static String[] fieldsFor(OrbType t) {
        return switch (t) {
            default -> throw new IncompatibleClassChangeError();
            case OrbType.GLASS -> GLASS_SPELLS;
            case OrbType.LIGHTNING -> LIGHTNING_SPELLS;
            case OrbType.FROST -> FROST_SPELLS;
            case OrbType.ION -> ION_SPELLS;
            case OrbType.DARK -> DARK_SPELLS;
        };
    }

    private List<AbstractSpell> spellsFor(OrbType t) {
        return this.spellCache.computeIfAbsent(t, k -> WizardSummon.spellPoolOf(DefectOrbs.fieldsFor(k)));
    }

    private boolean castIronSpell(ServerLevel sl, OrbType type, int chargeTicks, ServerPlayer target) {
        boolean cast;
        DefectCoreEntity castTarget;
        IMagicEntity m;
        List<AbstractSpell> pool = this.spellsFor(type);
        if (type == OrbType.DARK) {
            return false;
        }
        if (pool.isEmpty()) {
            if (type == OrbType.DARK) {
                FTBossMod.LOGGER.warn("[FTBoss][\u5145\u80fd\u7403] \u9ed1\u7403\u94c1\u9b54\u6cd5\u6c60\u4e3a\u7a7a(\u6cd5\u672f\u540d\u4e0e\u94c1\u9b54\u6cd5\u7248\u672c\u5bf9\u4e0d\u4e0a?) -> \u56de\u9000\u5230\u5185\u7f6e\u84c4\u529b\u4f24\u5bb3");
            }
            return false;
        }
        DefectCoreEntity defectCoreEntity = this.boss;
        IMagicEntity magic = defectCoreEntity instanceof IMagicEntity ? (m = (IMagicEntity)defectCoreEntity) : null;
        int phase = this.boss.getPhase();
        int echoBonus = this.echoForm ? 2 : 0;
        int chargeBonus = Math.min(this.charge / 4, 4);
        AbstractSpell spell = null;
        if (type == OrbType.DARK || this.echoForm) {
            List<AbstractSpell> pick;
            int want = 6 + phase + Math.min(chargeTicks / 60, 6) + echoBonus + chargeBonus;
            int minRarity = this.echoForm ? 4 : 3;
            ArrayList<AbstractSpell> rare = new ArrayList<AbstractSpell>();
            for (AbstractSpell s : pool) {
                if (WizardSummon.rarityOf(s, Math.min(want, s.getMaxLevel())) < minRarity) continue;
                rare.add(s);
            }
            if (rare.isEmpty() && this.echoForm) {
                for (AbstractSpell s : pool) {
                    if (WizardSummon.rarityOf(s, Math.min(want, s.getMaxLevel())) < 3) continue;
                    rare.add(s);
                }
            }
            if (rare.isEmpty()) {
                for (int extra = 2; extra <= 8 && rare.isEmpty(); extra += 2) {
                    for (AbstractSpell s : pool) {
                        if (WizardSummon.rarityOf(s, Math.min(want + extra, s.getMaxLevel())) < minRarity) continue;
                        rare.add(s);
                    }
                }
            }
            List<AbstractSpell> list = pick = rare.isEmpty() ? pool : rare;
            if (type == OrbType.DARK && this.boss.m_217043_().m_188501_() < 0.5f) {
                for (AbstractSpell s : pick) {
                    if (!"irons_spellbooks:black_hole".equals(s.getSpellId())) continue;
                    spell = s;
                    break;
                }
            }
            if (spell == null) {
                spell = pick.get(this.boss.m_217043_().m_188503_(pick.size()));
            }
        } else if (type == OrbType.LIGHTNING) {
            ArrayList<AbstractSpell> pref = new ArrayList<AbstractSpell>();
            for (AbstractSpell s : pool) {
                String id;
                String string = id = s.getSpellId() == null ? "" : s.getSpellId();
                if (!id.endsWith(":lightning_bolt") && !id.endsWith(":thunderstorm")) continue;
                pref.add(s);
            }
            List<AbstractSpell> pick = pref.isEmpty() ? pool : pref;
            spell = pick.get(this.boss.m_217043_().m_188503_(pick.size()));
        } else {
            spell = pool.get(this.boss.m_217043_().m_188503_(pool.size()));
        }
        int lvl = Math.max(spell.getMinLevel(), Math.min(4 + phase + echoBonus + chargeBonus + (type == OrbType.DARK ? 2 + Math.min(chargeTicks / 80, 5) : 0), spell.getMaxLevel()));
        switch (type) {
            case FROST: 
            case ION: {
                DefectCoreEntity defectCoreEntity2 = this.boss;
                break;
            }
            default: {
                DefectCoreEntity defectCoreEntity2 = castTarget = target != null ? target : this.boss;
            }
        }
        if (castTarget == null || castTarget == this.boss) {
            castTarget = this.nearest(sl, 48.0);
        }
        boolean bl = cast = magic != null ? WizardSummon.castAt(magic, (LivingEntity)castTarget, spell, lvl) : WizardSummon.castDirect((LivingEntity)this.boss, spell, lvl, (LivingEntity)castTarget);
        if (!cast) {
            return false;
        }
        this.FtLog(type, spell.getSpellId(), lvl, chargeTicks);
        if (this.echoForm && this.echoSpell == null) {
            this.echoSpell = spell;
            this.echoLevel = lvl;
            this.echoTarget = castTarget;
            this.echoTicks = 16;
        }
        return true;
    }

    private void fireEcho() {
        Level level;
        AbstractSpell spell = this.echoSpell;
        LivingEntity target = this.echoTarget;
        int lvl = this.echoLevel;
        this.echoSpell = null;
        if (spell == null || !((level = this.boss.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        DefectCoreEntity defectCoreEntity = this.boss;
        if (!(defectCoreEntity instanceof IMagicEntity)) {
            return;
        }
        IMagicEntity magic = (IMagicEntity)defectCoreEntity;
        if (!WizardSummon.castAt(magic, target, spell, lvl)) {
            return;
        }
        sl.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.4f, 0.6f);
        this.boss.playCastAnim(OrbType.DARK);
        this.broadcast("\u00a75\u3010\u56de\u60f3\u3011\u00a77 \u540c\u4e00\u6cd5\u672f\u518d\u6b21\u65bd\u653e: \u00a7d" + spell.getSpellId() + " \u00a77lv" + lvl);
        FTBossMod.LOGGER.info("[FTBoss][\u56de\u60f3\u5f62\u6001] \u590d\u8bfb\u6cd5\u672f {} lv{} -> {}", new Object[]{spell.getSpellId(), lvl, target == null ? "-" : target.m_7755_().getString()});
    }

    public boolean castBasicSpell(LivingEntity target) {
        String sid;
        DefectCoreEntity defectCoreEntity = this.boss;
        if (!(defectCoreEntity instanceof IMagicEntity)) {
            return false;
        }
        IMagicEntity magic = (IMagicEntity)defectCoreEntity;
        if (this.basicPool == null) {
            this.basicPool = WizardSummon.spellPoolOf(BASIC_SPELLS);
        }
        if (this.basicPool.isEmpty()) {
            return false;
        }
        AbstractSpell s = this.basicPool.get(this.boss.m_217043_().m_188503_(this.basicPool.size()));
        int lvl = Math.max(s.getMinLevel(), Math.min(3 + this.boss.getPhase() + (this.echoForm ? 2 : 0), s.getMaxLevel()));
        String string = sid = s.getSpellId() == null ? "" : s.getSpellId();
        OrbType anim = sid.contains("ice") || sid.contains("frost") ? OrbType.FROST : (sid.contains("fire") || sid.contains("flame") ? OrbType.GLASS : OrbType.LIGHTNING);
        this.boss.playCastAnim(anim);
        this.pendingBasicSpell = s;
        this.pendingBasicLevel = lvl;
        this.pendingBasicTarget = target;
        this.pendingBasicTicks = DefectOrbs.releaseDelay(anim);
        FTBossMod.LOGGER.info("[FTBoss][\u6545\u969c\u673a\u5668\u4eba] \u57fa\u7840\u653b\u51fb\u6cd5\u672f {} lv{} -> {}", new Object[]{s.getSpellId(), lvl, target.m_7755_().getString()});
        return true;
    }

    private void FtLog(OrbType type, String spellId, int lvl, int chargeTicks) {
        FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] {} \u6fc0\u53d1 -> \u94c1\u9b54\u6cd5 {} lv{}{} (\u84c4\u529b {}t, \u9636\u6bb5 {})", new Object[]{type.id, spellId, lvl, this.echoForm ? " [\u56de\u60f3\u5f62\u6001]" : "", chargeTicks, this.boss.getPhase()});
    }

    private void tickPassiveBuffs(List<ChargedOrbEntity> mine) {
        MobEffect[] mobEffectArray;
        if (!mine.isEmpty()) {
            this.boss.showOrbCue("passive", OrbType.ION, -1, 8);
        }
        int glass = 0;
        int lightning = 0;
        int frost = 0;
        int ion = 0;
        for (ChargedOrbEntity o : mine) {
            switch (o.getOrbType()) {
                case GLASS: {
                    ++glass;
                    break;
                }
                case LIGHTNING: {
                    ++lightning;
                    break;
                }
                case FROST: {
                    ++frost;
                    break;
                }
                case ION: {
                    ++ion;
                    break;
                }
            }
        }
        int phase = this.boss.getPhase();
        int gain = (glass + lightning * 2) * phase;
        if (gain > 0) {
            this.charge = Math.min(60, this.charge + gain);
        }
        if (this.charge >= 6 && (mobEffectArray = this.boss.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)mobEffectArray;
            while (this.charge >= 6) {
                this.charge -= 6;
                ServerPlayer t = this.nearest(sl, 32.0);
                if (t == null) break;
                this.strikeLightning(sl, t, phase);
            }
        }
        if (frost > 0) {
            this.boss.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 60, Math.min((frost - 1) / 2 + (phase - 1) / 2, 1), false, false, true));
            this.boss.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 90, 0, false, false, true));
        }
        if (ion > 0) {
            int amp = Math.min(ion - 1 + (phase - 1), 3);
            for (MobEffect fx : new MobEffect[]{MobEffects.f_19596_, MobEffects.f_19598_, MobEffects.f_19603_, MobEffects.f_19621_, MobEffects.f_19618_, MobEffects.f_19607_, MobEffects.f_19611_, MobEffects.f_19608_, MobEffects.f_19593_, MobEffects.f_19591_, MobEffects.f_19592_}) {
                this.boss.m_7292_(new MobEffectInstance(fx, 90, amp, false, false, true));
            }
        }
        if (glass + lightning + frost + ion > 0 && !this.passiveLogged) {
            this.passiveLogged = true;
            FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u7403\u88ab\u52a8\u751f\u6548: \u73bb\u7483{} \u7535{} \u51b0{} \u79bb\u5b50{} | \u9636\u6bb5{} \u5145\u80fd{}/{} | \u6bcf\u6b21\u95ea\u7535\u4f24\u5bb3 {}", new Object[]{glass, lightning, frost, ion, phase, this.charge, 60, 12 + phase * 6});
        }
    }

    private void strikeLightning(ServerLevel sl, ServerPlayer t, int phase) {
        try {
            LightningBolt bolt = (LightningBolt)EntityType.f_20465_.m_20615_((Level)sl);
            if (bolt != null) {
                bolt.m_6027_(t.m_20185_(), t.m_20186_(), t.m_20189_());
                bolt.m_20874_(true);
                sl.m_7967_((Entity)bolt);
            }
        }
        catch (Throwable bolt) {
            // empty catch block
        }
        for (ChargedOrbEntity o : this.collectOrbs(sl)) {
            if (o.getOrbType() != OrbType.LIGHTNING) continue;
            o.beginAssault();
        }
        float dmg = BossScaling.dmg((LivingEntity)this.boss, 12.0f + (float)phase * 6.0f);
        t.f_19802_ = 0;
        this.boss.playCastAnim(OrbType.LIGHTNING);
        t.m_6469_(sl.m_269111_().m_269333_((LivingEntity)this.boss), dmg * 0.25f);
        t.f_19802_ = 0;
        t.m_6469_(sl.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), dmg * 0.25f);
        sl.m_6263_(null, t.m_20185_(), t.m_20186_(), t.m_20189_(), SoundEvents.f_12090_, SoundSource.HOSTILE, 1.2f, 1.2f);
        this.broadcast("\u00a7e\u3010\u5145\u80fd\u00b7\u95ea\u7535\u6253\u51fb\u3011\u00a77 \u5145\u80fd\u5df2\u6ee1 -> " + String.format("%.1f", Float.valueOf(dmg)) + " \u70b9\u96f7\u7535\u4f24\u5bb3");
        FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u95ea\u7535\u6253\u51fb -> {} ({} \u4f24\u5bb3, \u9636\u6bb5 {})", new Object[]{t.m_7755_().getString(), String.format("%.1f", Float.valueOf(dmg)), phase});
    }

    public int charge() {
        return this.charge;
    }

    public boolean isEchoForm() {
        return this.echoForm;
    }

    public DefectOrbs(DefectCoreEntity boss) {
        this.boss = boss;
    }

    public int slots() {
        return this.orbSlots;
    }

    public void unlockPhase(int phase) {
        if (phase <= this.unlockedPhases) {
            return;
        }
        this.unlockedPhases = phase;
        int before = this.orbSlots;
        this.orbSlots = Math.min(11, 5 + 3 * (phase - 1));
        this.boss.playPhaseAnim();
        if (this.orbSlots != before) {
            FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u9636\u6bb5 {} \u89e3\u9501: \u7403\u69fd {} -> {}", new Object[]{phase, before, this.orbSlots});
            Level level = this.boss.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                this.broadcast("\u00a75\u6545\u969c\u673a\u5668\u4eba\u00a77 \u7403\u69fd\u6269\u5c55\u81f3 \u00a7d" + this.orbSlots + "\u00a77 \u4e2a");
            }
        }
    }

    public void onOrbDestroyed(OrbType type) {
        this.punishCd = 40;
    }

    public void onOrbDestroyed(ChargedOrbEntity orb) {
        this.onOrbDestroyed(orb.getOrbType());
        this.castBacklog.removeIf(queued -> orb.m_20148_().equals(queued.orbId()));
        if (orb.m_20148_().equals(this.pendingOrbId)) {
            this.pendingType = null;
            this.pendingOrbId = null;
            this.pendingTicks = 0;
            this.boss.cancelOrbCastVisual();
            this.boss.showOrbCue("interrupt", orb.getOrbType(), orb.m_19879_(), 8);
        }
    }

    public void onOrbMatured(ChargedOrbEntity orb) {
        this.requestOrbCast(orb);
    }

    private void requestOrbCast(ChargedOrbEntity orb) {
        if (!orb.isOrbAvailable() || !orb.isOwnedBy(this.boss) || orb.isReservedForCast() || orb.isSpentProjectile()) {
            return;
        }
        if (this.castBacklog.size() >= 11) {
            return;
        }
        orb.reserveForCast();
        if (this.pendingType != null || this.boss.isActionLocked()) {
            this.castBacklog.addLast(new QueuedCast(orb.getOrbType(), orb.getChargeTicks(), orb.m_20148_()));
        } else {
            this.beginOrbCast(orb.getOrbType(), orb.getChargeTicks(), orb);
        }
    }

    private ChargedOrbEntity findOrb(UUID orbId) {
        ChargedOrbEntity orb;
        Level level;
        if (orbId == null || !((level = this.boss.m_9236_()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel server = (ServerLevel)level;
        Entity entity = server.m_8791_(orbId);
        return entity instanceof ChargedOrbEntity && (orb = (ChargedOrbEntity)entity).isOrbAvailable() && orb.isOwnedBy(this.boss) ? orb : null;
    }

    public void tick() {
        boolean hadOrbs;
        int cycleLen;
        boolean wantEcho;
        if (this.boss.m_21224_()) {
            return;
        }
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (this.pendingOrbId != null && this.findOrb(this.pendingOrbId) == null) {
            this.pendingType = null;
            this.pendingOrbId = null;
            this.boss.cancelOrbCastVisual();
        }
        if (this.pendingBasicSpell != null && --this.pendingBasicTicks <= 0) {
            AbstractSpell spell = this.pendingBasicSpell;
            LivingEntity target = this.pendingBasicTarget;
            this.pendingBasicSpell = null;
            this.pendingBasicTarget = null;
            DefectCoreEntity defectCoreEntity = this.boss;
            if (defectCoreEntity instanceof IMagicEntity) {
                IMagicEntity magic = (IMagicEntity)defectCoreEntity;
                if (!this.boss.isExhausted() && target != null && target.m_6084_() && target.m_9236_() == sl && BossTargets.valid(target) && this.boss.m_20280_((Entity)target) <= 676.0 && this.boss.m_142582_((Entity)target)) {
                    WizardSummon.castAt(magic, target, spell, this.pendingBasicLevel);
                }
            }
        }
        if (this.pendingType != null && --this.pendingTicks <= 0) {
            OrbType t = this.pendingType;
            int c = this.pendingCharge;
            ChargedOrbEntity selected = this.findOrb(this.pendingOrbId);
            this.pendingType = null;
            this.pendingOrbId = null;
            this.boss.showOrbCue("release", t, selected == null ? -1 : selected.m_19879_(), 12);
            if (t == OrbType.LIGHTNING) {
                ++this.lightningUsed;
            }
            if (selected != null && t == OrbType.DARK) {
                selected.releaseDark(c, this.orbDamageMul, (LivingEntity)this.nearest(sl, 28.0));
                sl.m_6263_(null, selected.m_20185_(), selected.m_20186_(), selected.m_20189_(), SoundEvents.f_12558_, SoundSource.HOSTILE, 1.6f, 0.8f);
            } else {
                if (selected != null) {
                    selected.absorbed();
                }
                this.releaseCast(t, c);
            }
            this.countSkillAndMaybeExhaust();
        }
        if (this.pendingType == null && !this.boss.isActionLocked() && !this.castBacklog.isEmpty()) {
            QueuedCast next = this.castBacklog.removeFirst();
            ChargedOrbEntity selected = this.findOrb(next.orbId());
            if (next.orbId() == null || selected != null) {
                this.beginOrbCast(next.type(), next.charge(), selected);
            }
        }
        if (this.boss.m_5448_() == null) {
            return;
        }
        if ((this.beamCharge > 0 || this.beamFire > 0 || !this.boss.isActionLocked()) && this.tickBeam(sl)) {
            return;
        }
        if (this.boss.isExhausted()) {
            return;
        }
        if (this.boss.isActionLocked()) {
            return;
        }
        if (this.tickCurrentIntergrowth(sl)) {
            return;
        }
        if (!this.fragmentsUsed && this.boss.getPhase() >= 2) {
            this.fragmentsUsed = true;
            this.orbDamageMul = 1.5f;
            this.maggotBase = 5;
            this.currentCd = Math.min(this.currentCd, 200);
            this.boss.playCastAnim(OrbType.ION);
            this.boss.showOrbCue("echo", OrbType.ION, -1, 20);
            sl.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 2.0f, 0.7f);
            this.broadcast("\u00a7d\u3010\u96c6\u4e2d\u788e\u7247\u3011\u00a77 \u6838\u5fc3\u628a\u788e\u7247\u5168\u90e8\u541e\u4e0b \u2014\u2014 \u00a7c\u5168\u90e8\u7403\u4f24\u5bb3 +50%\u00a77, \u53ec\u5524\u7684\u86c6\u4e5f\u66f4\u591a\u4e86!");
            FTBossMod.LOGGER.info("[FTBoss][\u96c6\u4e2d\u788e\u7247] \u5df2\u4f7f\u7528(\u4ec5\u4e00\u6b21): \u7403\u4f24\u5bb3 \u00d71.5, \u86c6\u57fa\u6570 {}", (Object)this.maggotBase);
            return;
        }
        if (this.boss.getPhase() >= 2 && --this.maggotCd <= 0) {
            MaggotMinionEntity mag;
            this.maggotCd = 240;
            int n = this.maggotBase + this.boss.m_217043_().m_188503_(2);
            for (int i = 0; i < n && (mag = (MaggotMinionEntity)((EntityType)FTEntities.MAGGOT_MINION.get()).m_20615_((Level)sl)) != null; ++i) {
                double a = this.boss.m_217043_().m_188500_() * Math.PI * 2.0;
                mag.setOwnerBoss(this.boss);
                mag.m_7678_(this.boss.m_20185_() + Math.cos(a) * 2.0, this.boss.m_20186_() + 0.2, this.boss.m_20189_() + Math.sin(a) * 2.0, this.boss.m_217043_().m_188501_() * 360.0f, 0.0f);
                sl.m_7967_((Entity)mag);
            }
            this.broadcast("\u00a76\u3010\uff01?\u533a\u533a\uff1f\uff01\u3011\u00a77 \u6838\u5fc3\u5410\u51fa\u4e86 " + n + " \u53ea\u86c6 \u2014\u2014 \u88ab\u54ac\u4f1a\u53e0\u3010\u611f\u67d3\u3011(\u653b\u51fb\u654c\u4eba\u65f6\u53cd\u566c)");
            FTBossMod.LOGGER.info("[FTBoss][\u533a\u533a] \u53ec\u5524\u86c6\u5c0f\u602a x{}", (Object)n);
        }
        if (this.punishCd > 0) {
            --this.punishCd;
        }
        if (--this.passiveCd <= 0) {
            this.passiveCd = 20;
            this.tickPassiveBuffs(this.collectOrbs(sl));
        }
        boolean bl = wantEcho = BossRage.tierOf((LivingEntity)this.boss) >= 1 || this.boss.getPhase() >= 2;
        if (wantEcho != this.echoForm) {
            this.echoForm = wantEcho;
            if (wantEcho) {
                sl.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_215771_, SoundSource.HOSTILE, 1.2f, 0.7f);
                this.boss.playEchoAnim();
                this.broadcast("\u00a75\u6545\u969c\u673a\u5668\u4eba \u00a7d\u8fdb\u5165\u3010\u56de\u60f3\u5f62\u6001\u3011\u00a77 \u2014\u2014 \u65bd\u6cd5\u53d8\u6162, \u4f46\u6bcf\u6b21\u53cc\u53d1, \u6cd5\u672f\u7b49\u7ea7\u4e0e\u7a00\u6709\u5ea6\u63d0\u5347");
                FTBossMod.LOGGER.info("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u8fdb\u5165\u3010\u56de\u60f3\u5f62\u6001\u3011(\u72c2\u66b4/\u4e8c\u9636\u6bb5): \u5468\u671f 40t -> {}t, \u6cd5\u672f\u53cc\u53d1+\u7b49\u7ea7+2", (Object)80);
            } else {
                this.broadcast("\u00a77\u6545\u969c\u673a\u5668\u4eba \u00a77\u9000\u51fa\u4e86\u3010\u56de\u60f3\u5f62\u6001\u3011");
                FTBossMod.LOGGER.info("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u9000\u51fa\u3010\u56de\u60f3\u5f62\u6001\u3011");
            }
        }
        if (this.boss.isActionLocked()) {
            return;
        }
        if (this.echoTicks > 0 && --this.echoTicks <= 0) {
            this.fireEcho();
        }
        int n = cycleLen = this.echoForm ? 80 : 48;
        if (--this.cycleCd > 0) {
            return;
        }
        this.cycleCd = cycleLen;
        List<ChargedOrbEntity> mine = this.collectOrbs(sl);
        boolean bl2 = hadOrbs = !mine.isEmpty();
        if (this.punishCd <= 0) {
            int want = Math.min(this.orbSlots, 11);
            int add = Math.max(0, want - mine.size());
            for (int i = 0; i < add; ++i) {
                this.summonOrb(sl, i);
            }
        }
        if (hadOrbs) {
            ChargedOrbEntity oldest = null;
            double best = -1.0;
            for (ChargedOrbEntity o : mine) {
                double ratio;
                if (o.isReservedForCast() || o.isSpentProjectile() || !((ratio = (double)o.getChargeTicks() / (double)o.getLifeTicks()) > best)) continue;
                best = ratio;
                oldest = o;
            }
            if (oldest != null) {
                this.requestOrbCast(oldest);
            }
        }
    }

    private void summonOrb(ServerLevel sl, int index) {
        int slot;
        ChargedOrbEntity orb = (ChargedOrbEntity)((EntityType)FTEntities.CHARGED_ORB.get()).m_20615_((Level)sl);
        if (orb == null) {
            return;
        }
        OrbType type = this.rollType();
        orb.setOrbType(type);
        orb.setOwnerBoss(this.boss);
        boolean[] occupied = new boolean[Math.max(1, this.orbSlots)];
        for (ChargedOrbEntity existing : this.collectOrbs(sl)) {
            int slot2 = existing.formationSlot();
            if (slot2 < 0 || slot2 >= occupied.length) continue;
            occupied[slot2] = true;
        }
        for (slot = 0; slot < occupied.length - 1 && occupied[slot]; ++slot) {
        }
        orb.setFormationSlot(slot, this.orbSlots);
        double a = Math.PI * 2 / (double)Math.max(1, this.orbSlots) * (double)index;
        orb.m_6034_(this.boss.m_20185_() + Math.cos(a) * 1.4, this.boss.m_20186_() + 1.6, this.boss.m_20189_() + Math.sin(a) * 1.4);
        orb.setLifeTicks(400 + (this.boss.getPhase() - 1) * 80 + (type == OrbType.DARK ? 240 : 0));
        sl.m_7967_((Entity)orb);
        this.boss.playSummonAnim();
        this.boss.showOrbCue("summon", type, orb.m_19879_(), 17);
        this.boss.setCoreTint(type);
        sl.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.2f, 1.3f);
        FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u751f\u6210 {} (\u69fd\u4f4d {}/{})", new Object[]{type.id, this.collectOrbs(sl).size(), this.orbSlots});
    }

    private void summonOrb(ServerLevel sl) {
        this.summonOrb(sl, 0);
    }

    private OrbType rollType() {
        int idx = Math.max(0, Math.min(2, this.boss.getPhase() - 1));
        int[] w = ORB_WEIGHTS[idx];
        int total = 0;
        for (int x : w) {
            total += x;
        }
        int r = this.boss.m_217043_().m_188503_(total);
        for (int i = 0; i < w.length; ++i) {
            if ((r -= w[i]) >= 0) continue;
            return OrbType.values()[i];
        }
        return OrbType.GLASS;
    }

    public void absorbAndCast(OrbType type, int chargeTicks) {
        if (!this.boss.m_6084_() || this.boss.m_9236_().f_46443_) {
            return;
        }
        if (this.pendingType != null || this.boss.isActionLocked()) {
            if (this.castBacklog.size() < 11) {
                this.castBacklog.addLast(new QueuedCast(type, chargeTicks, null));
            }
            return;
        }
        this.beginOrbCast(type, chargeTicks, null);
    }

    private void beginOrbCast(OrbType type, int chargeTicks, ChargedOrbEntity selected) {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        this.boss.playCastAnim(type);
        this.boss.setCoreTint(type);
        this.pendingType = type;
        this.pendingCharge = chargeTicks;
        this.pendingTicks = DefectOrbs.releaseDelay(type);
        UUID uUID = this.pendingOrbId = selected == null ? null : selected.m_20148_();
        if (selected != null) {
            selected.focusForCast(this.pendingTicks);
        }
        this.boss.showOrbCue("focus", type, selected == null ? -1 : selected.m_19879_(), this.pendingTicks);
        sl.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_() + 1.6, this.boss.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.0f, 0.75f);
    }

    private static int releaseDelay(OrbType type) {
        return OrbLinkMath.releaseTicks(type.id);
    }

    private void launchDarkOrb(ServerLevel sl, int chargeTicks, ServerPlayer target) {
        double bx = this.boss.m_20185_();
        double by = this.boss.m_20186_() + 1.6;
        double bz = this.boss.m_20189_();
        float bonus = 2.0f * ((float)chargeTicks / 20.0f);
        ChargedOrbEntity orb = (ChargedOrbEntity)((EntityType)FTEntities.CHARGED_ORB.get()).m_20615_((Level)sl);
        if (orb == null) {
            return;
        }
        double ax = target != null ? target.m_20185_() - bx : -Math.sin(Math.toRadians(this.boss.m_146908_()));
        double az = target != null ? target.m_20189_() - bz : Math.cos(Math.toRadians(this.boss.m_146908_()));
        double al = Math.max(0.001, Math.sqrt(ax * ax + az * az));
        orb.setOrbType(OrbType.DARK);
        orb.setOwnerBoss(this.boss);
        orb.setSlamBonus(bonus);
        orb.setDamageMul(this.orbDamageMul);
        orb.setBlackHoleChance(true);
        orb.setLifeTicks(200);
        orb.m_6034_(bx + ax / al * 1.6, by, bz + az / al * 1.6);
        orb.releaseDark(chargeTicks, this.orbDamageMul, (LivingEntity)target);
        sl.m_7967_((Entity)orb);
        this.boss.playCastAnim(OrbType.DARK);
        sl.m_6263_(null, bx, by, bz, SoundEvents.f_12558_, SoundSource.HOSTILE, 1.6f, 0.8f);
        this.broadcast("\u00a75\u3010\u9ed1\u7403\u00b7\u53d1\u5c04\u3011\u00a77 \u84c4\u529b " + chargeTicks / 20 + " \u79d2 -> \u649e\u51fb\u4f24\u5bb3 \u00a7c+" + (int)bonus);
        FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u7403] \u53d1\u5c04\u649e\u51fb: \u84c4\u529b {}t, \u989d\u5916\u4f24\u5bb3 +{}", (Object)chargeTicks, (Object)((int)bonus));
    }

    private void releaseCast(OrbType type, int chargeTicks) {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        double bx = this.boss.m_20185_();
        double by = this.boss.m_20186_() + 1.6;
        double bz = this.boss.m_20189_();
        ServerPlayer target = this.nearest(sl, 28.0);
        Fx.particles((Level)sl, bx, by, bz, 20, (ParticleOptions)ParticleTypes.f_123810_, 0.45, 0.16);
        Fx.ring((Level)sl, bx, this.boss.m_20186_() + 0.4, bz, 2.2, 20, (ParticleOptions)ParticleTypes.f_123797_, 0.05);
        if (type == OrbType.DARK) {
            this.launchDarkOrb(sl, chargeTicks, target);
            return;
        }
        boolean spellCast = this.castIronSpell(sl, type, chargeTicks, target);
        switch (type) {
            case GLASS: {
                if (!spellCast) {
                    int hits = 0;
                    for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(8.0))) {
                        if (!p.m_6084_()) continue;
                        p.f_19802_ = 0;
                        p.m_6469_(sl.m_269111_().m_269333_((LivingEntity)this.boss), BossScaling.dmg((LivingEntity)this.boss, 20.0f * this.orbDamageMul) * 0.25f);
                        p.f_19802_ = 0;
                        p.m_6469_(sl.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), BossScaling.dmg((LivingEntity)this.boss, 20.0f * this.orbDamageMul) * 0.25f);
                        p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0, false, true, true));
                        ++hits;
                    }
                    FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u73bb\u7483\u7403(\u5185\u7f6eAOE\u56de\u9000), \u547d\u4e2d {} \u4eba", (Object)hits);
                }
                sl.m_6263_(null, bx, by, bz, SoundEvents.f_11983_, SoundSource.HOSTILE, 2.4f, 0.6f);
                this.broadcast("\u00a7b\u3010\u73bb\u7483\u7403\u00b7\u8303\u56f4\u6cd5\u672f\u3011\u00a77 " + (spellCast ? "\u94c1\u9b54\u6cd5\u5df2\u53d1\u52a8" : "\u8303\u56f4\u788e\u88c2"));
                break;
            }
            case LIGHTNING: {
                if (target == null) {
                    this.broadcast("\u00a7e\u3010\u7535\u7403\u00b7\u96f7\u51fb\u3011\u00a77 \u627e\u4e0d\u5230\u76ee\u6807, \u7a7a\u653e");
                    break;
                }
                if (!spellCast) {
                    target.f_19802_ = 0;
                    target.m_6469_(sl.m_269111_().m_269333_((LivingEntity)this.boss), BossScaling.dmg((LivingEntity)this.boss, 30.0f * this.orbDamageMul) * 0.25f);
                    target.f_19802_ = 0;
                    target.m_6469_(sl.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), BossScaling.dmg((LivingEntity)this.boss, 30.0f * this.orbDamageMul) * 0.25f);
                    target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 50, 2, false, true, true));
                    FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u7535\u7403(\u5185\u7f6e\u96f7\u51fb\u56de\u9000) -> {}", (Object)target.m_7755_().getString());
                }
                if (target != null) {
                    sl.m_6263_(null, target.m_20185_(), target.m_20186_(), target.m_20189_(), SoundEvents.f_12089_, SoundSource.HOSTILE, 1.4f, 1.3f);
                }
                this.broadcast("\u00a7e\u3010\u7535\u7403\u00b7\u5355\u4f53\u96f7\u51fb\u3011\u00a77 \u9501\u5b9a " + target.m_7755_().getString() + (spellCast ? " (\u94c1\u9b54\u6cd5)" : ""));
                break;
            }
            case DARK: {
                if (!spellCast) {
                    ServerPlayer t = target;
                    float dmg = BossScaling.dmg((LivingEntity)this.boss, (16.0f + Math.min((float)chargeTicks / 14.0f, 30.0f)) * this.orbDamageMul);
                    if (t != null) {
                        t.f_19802_ = 0;
                        t.m_6469_(sl.m_269111_().m_269333_((LivingEntity)this.boss), dmg * 0.25f);
                        t.f_19802_ = 0;
                        t.m_6469_(sl.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), dmg * 0.25f);
                        FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u9ed1\u7403(\u5185\u7f6e\u84c4\u529b\u56de\u9000) {}t -> {} \u4f24\u5bb3 {}", new Object[]{chargeTicks, t.m_7755_().getString(), String.format("%.1f", Float.valueOf(dmg))});
                    }
                }
                if (target != null) {
                    sl.m_6263_(null, target.m_20185_(), target.m_20186_(), target.m_20189_(), SoundEvents.f_215750_, SoundSource.HOSTILE, 1.2f, 1.1f);
                }
                this.broadcast("\u00a75\u3010\u9ed1\u7403\u00b7\u84c4\u529b\u7206\u53d1\u3011\u00a77 \u84c4\u529b " + chargeTicks / 20 + "s \u2192 \u9ad8\u7a00\u6709\u5ea6\u6cd5\u672f" + (spellCast ? " (\u94c1\u9b54\u6cd5)" : ""));
                break;
            }
            case FROST: {
                this.boss.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 200, 0, false, false, true));
                sl.m_6263_(null, bx, by, bz, SoundEvents.f_11986_, SoundSource.HOSTILE, 1.6f, 0.8f);
                this.broadcast("\u00a7f\u3010\u51b0\u7403\u00b7\u4fdd\u62a4\u3011\u00a77 \u971c\u7532(\u5438\u6536I)" + (spellCast ? " + \u94c1\u9b54\u6cd5\u51b0\u7cfb" : ""));
                break;
            }
            case ION: {
                this.charge = Math.min(60, this.charge + 12);
                this.boss.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 260, 1, false, false, true));
                sl.m_6263_(null, bx, by, bz, SoundEvents.f_11739_, SoundSource.HOSTILE, 1.8f, 1.5f);
                this.broadcast("\u00a7d\u3010\u79bb\u5b50\u7403\u00b7\u5f3a\u5316\u3011\u00a77 \u5145\u80fd +12(\u6cd5\u672f\u7b49\u7ea7+\u95ea\u7535\u6253\u51fb) + \u901f\u5ea6II" + (spellCast ? " + \u94c1\u9b54\u6cd5\u589e\u76ca" : ""));
            }
        }
    }

    public void reset() {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        for (ChargedOrbEntity orb : sl.m_45976_(ChargedOrbEntity.class, this.boss.m_20191_().m_82400_(64.0))) {
            if (!orb.isOwnedBy(this.boss)) continue;
            orb.m_146870_();
        }
        this.castBacklog.clear();
        this.pendingType = null;
        this.pendingOrbId = null;
        this.pendingBasicSpell = null;
        this.echoSpell = null;
        this.boss.showOrbCue("", OrbType.GLASS, -1, 1);
        this.orbSlots = 5;
        this.unlockedPhases = 1;
    }

    public List<ChargedOrbEntity> collectOrbs(ServerLevel sl) {
        ArrayList<ChargedOrbEntity> out = new ArrayList<ChargedOrbEntity>();
        for (ChargedOrbEntity o : sl.m_45976_(ChargedOrbEntity.class, this.boss.m_20191_().m_82400_(48.0))) {
            if (!o.isOrbAvailable() || !o.isOwnedBy(this.boss)) continue;
            out.add(o);
        }
        out.sort((a, b) -> Integer.compare(b.getChargeTicks(), a.getChargeTicks()));
        return out;
    }

    private boolean tickBeam(ServerLevel sl) {
        double x = this.boss.m_20185_();
        double y = this.boss.m_20186_() + 1.7;
        double z = this.boss.m_20189_();
        if (this.beamFire > 0) {
            ServerPlayer lockOn = this.nearest(sl, 40.0);
            if (lockOn != null && lockOn.m_6084_()) {
                double lx = lockOn.m_20185_() - x;
                double ly = lockOn.m_20186_() + (double)lockOn.m_20206_() * 0.5 - y;
                double lz = lockOn.m_20189_() - z;
                double ll = Math.max(0.001, Math.sqrt(lx * lx + ly * ly + lz * lz));
                this.beamDx = lx / ll;
                this.beamDy = ly / ll;
                this.beamDz = lz / ll;
            }
            this.beamFx(sl);
            if (--this.beamPulse <= 0) {
                this.beamPulse = 10;
                this.beamDamage(sl);
            }
            if (--this.beamFire <= 0) {
                sl.m_6263_(null, x, y, z, SoundEvents.f_11738_, SoundSource.HOSTILE, 1.6f, 0.6f);
                this.broadcast("\u00a7b\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a77 \u5f15\u5bfc\u7ed3\u675f");
                this.countSkillAndMaybeExhaust();
                FTBossMod.LOGGER.info("[FTBoss][\u8d85\u80fd\u5149\u675f] \u5f15\u5bfc\u7ed3\u675f(\u5171 {} \u6b21\u8109\u51b2, \u6bcf\u6b21 {} \u4f24\u5bb3)", (Object)6, (Object)String.format("%.1f", Float.valueOf(22.0f)));
                this.beamCd = this.beamCooldown();
                this.boss.setBeamCharging(false);
                this.boss.enterExhausted(120);
                this.broadcast("\u00a7c\u3010\u8fc7\u8f7d\u8870\u5f31\u3011\u00a77 \u6838\u5fc3\u843d\u5730, \u671f\u95f4\u53d7\u5230 \u00a7c1.5\u00d7\u00a77 \u4f24\u5bb3 \u2014\u2014 \u53cd\u51fb\u65f6\u673a!");
                FTBossMod.LOGGER.info("[FTBoss][\u8d85\u80fd\u5149\u675f] \u8fdb\u5165\u843d\u5730\u8870\u5f31 {}t", (Object)120);
            }
            return true;
        }
        if (this.beamCharge > 0) {
            Fx.particles((Level)sl, x + this.beamDx * 0.6, y + this.beamDy * 0.6, z + this.beamDz * 0.6, 3, (ParticleOptions)ParticleTypes.f_123789_, 0.45, 0.03);
            double prog = 1.0 - (double)this.beamCharge / 160.0;
            DustParticleOptions warnRed = new DustParticleOptions(new Vector3f(1.0f, 0.05f, 0.05f), 1.35f);
            Fx.ring((Level)sl, x, this.boss.m_20186_() + 0.2, z, 4.6 - 3.4 * prog, 8, (ParticleOptions)warnRed, 0.0);
            for (double d = 1.0; d <= 40.0 * (0.3 + 0.7 * prog); d += 3.0) {
                Fx.particles((Level)sl, x + this.beamDx * d, y + this.beamDy * d, z + this.beamDz * d, 1, (ParticleOptions)warnRed, 0.06, 0.0);
            }
            Fx.particles((Level)sl, x, y, z, 3, (ParticleOptions)warnRed, 0.6, 0.02);
            if (this.beamCharge % 8 == 0) {
                sl.m_6263_(null, x, y, z, SoundEvents.f_11739_, SoundSource.HOSTILE, 1.1f, 0.7f + (float)prog * 0.7f);
            }
            if (--this.beamCharge <= 0) {
                this.beginBeamFire(sl);
            }
            return true;
        }
        if (this.beamCd > 0 && --this.beamCd <= 0) {
            ServerPlayer t = this.nearest(sl, 24.0);
            if (t != null && this.pendingType == null) {
                double ty2 = t.m_20186_() + (double)t.m_20206_() * 0.5;
                double dx2 = t.m_20185_() - x;
                double dy2 = ty2 - y;
                double dz2 = t.m_20189_() - z;
                double len = Math.max(0.001, Math.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2));
                this.beamDx = dx2 / len;
                this.beamDy = dy2 / len;
                this.beamDz = dz2 / len;
                this.beamCharge = 160;
                this.beamPulse = 10;
                this.boss.playBeamAnim();
                this.boss.showOrbCue("beam_charge", OrbType.ION, -1, 160);
                this.boss.setBeamCharging(true);
                for (MobEffect fx : new MobEffect[]{MobEffects.f_19606_, MobEffects.f_19617_, MobEffects.f_19607_, MobEffects.f_19616_}) {
                    this.boss.m_21195_(fx);
                }
                for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(40.0))) {
                    if (!p.m_6084_()) continue;
                    p.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 190, 0, false, true, true));
                    p.m_5661_((Component)Component.m_237113_((String)"\u00a7b\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a77\u6838\u5fc3\u6b63\u5728\u84c4\u529b \u2014\u2014 \u00a7a\u4f60\u83b7\u5f97\u52a0\u901f I\u00a77, \u5feb\u79bb\u5f00\u5c04\u7ebf!"), true);
                }
                sl.m_6263_(null, x, y, z, SoundEvents.f_11867_, SoundSource.HOSTILE, 1.5f, 0.7f);
                this.broadcast("\u00a7b\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a77 \u6838\u5fc3\u5f00\u59cb\u84c4\u80fd \u2014\u2014 \u00a7c\u79bb\u5f00\u5c04\u7ebf!");
                FTBossMod.LOGGER.info("[FTBoss][\u8d85\u80fd\u5149\u675f] \u84c4\u529b\u5f00\u59cb, \u9501\u5b9a\u65b9\u5411 ({}, {}, {})", new Object[]{String.format("%.2f", this.beamDx), String.format("%.2f", this.beamDy), String.format("%.2f", this.beamDz)});
                return true;
            }
            this.beamCd = 40;
        }
        return false;
    }

    private int beamCooldown() {
        int phase = this.boss.getPhase();
        int base = phase >= 3 ? 200 : (phase == 2 ? 280 : 360);
        return this.echoForm ? (int)((double)base * 0.7) : base;
    }

    private void beginBeamFire(ServerLevel sl) {
        double x = this.boss.m_20185_();
        double y = this.boss.m_20186_() + 1.7;
        double z = this.boss.m_20189_();
        this.beamFire = 60;
        this.boss.showOrbCue("beam_fire", OrbType.ION, -1, 60);
        this.beamPulse = 10;
        this.boss.setBeamCharging(false);
        sl.m_6263_(null, x, y, z, SoundEvents.f_11736_, SoundSource.HOSTILE, 2.0f, 1.6f);
        this.broadcast("\u00a7b\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a77 \u5c04\u7ebf\u5c06\u6301\u7eed 3 \u79d2 \u2014\u2014 \u00a7c\u5c3d\u5feb\u79bb\u5f00\u5c04\u7ebf!");
        this.beamDamage(sl);
    }

    private void beamDamage(ServerLevel sl) {
        Vec3 from = new Vec3(this.boss.m_20185_(), this.boss.m_20186_() + 1.7, this.boss.m_20189_());
        Vec3 dir = new Vec3(this.beamDx, this.beamDy, this.beamDz).m_82541_();
        AABB box = new AABB(from, from.m_82549_(dir.m_82490_(40.0))).m_82400_(5.0);
        int hits = 0;
        for (LivingEntity le : sl.m_45976_(LivingEntity.class, box)) {
            double perp;
            Vec3 rel;
            double along;
            if (le == this.boss || !le.m_6084_() || le instanceof DefectCoreEntity || (along = (rel = le.m_20182_().m_82520_(0.0, (double)le.m_20206_() * 0.5, 0.0).m_82546_(from)).m_82526_(dir)) < -0.5 || along > 40.0 || (perp = rel.m_82546_(dir.m_82490_(along)).m_82553_()) > 3.5 + (double)le.m_20205_() * 0.5) continue;
            float bTotal = BossScaling.dmg((LivingEntity)this.boss, 22.0f);
            float bQuarter = bTotal / 8.0f;
            le.f_19802_ = 0;
            le.m_6469_(sl.m_269111_().m_269333_((LivingEntity)this.boss), bQuarter);
            le.f_19802_ = 0;
            le.m_6469_(sl.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), bQuarter);
            le.f_19802_ = 0;
            le.m_6469_(sl.m_269111_().m_269341_(), bQuarter);
            le.f_19802_ = 0;
            le.m_6469_(this.beamDamageSource(sl), bQuarter);
            Vec3 push = dir.m_82490_(0.32);
            BossKnockback.push((Entity)le, push.f_82479_, 0.06, push.f_82481_);
            ++hits;
        }
        sl.m_6263_(null, from.f_82479_, from.f_82480_, from.f_82481_, SoundEvents.f_215772_, SoundSource.HOSTILE, 1.3f, 1.5f);
        if (hits > 0) {
            FTBossMod.LOGGER.info("[FTBoss][\u8d85\u80fd\u5149\u675f] \u547d\u4e2d {} \u4e2a\u751f\u7269, \u6bcf\u4e2a {} \u4f24\u5bb3", (Object)hits, (Object)String.format("%.1f", Float.valueOf(22.0f)));
        }
    }

    private DamageSource beamDamageSource(ServerLevel sl) {
        if (!TRUE_DMG_TRIED) {
            TRUE_DMG_TRIED = true;
            Registry reg = sl.m_9598_().m_175515_(Registries.f_268580_);
            for (String p : new String[]{"electrocution", "assassination", "hemorrhage", "frenzied_burn", "venom", "cacophony", "doom"}) {
                ResourceKey key = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("armamentarium", p));
                if (!reg.m_142003_(key)) continue;
                TRUE_DMG_KEY = key;
                FTBossMod.LOGGER.info("[FTBoss][\u8d85\u80fd\u5149\u675f] \u4f24\u5bb3\u7c7b\u578b = \u771f\u5b9e\u4f24\u5bb3 armamentarium:{}", (Object)p);
                break;
            }
            if (TRUE_DMG_KEY == null) {
                FTBossMod.LOGGER.warn("[FTBoss][\u8d85\u80fd\u5149\u675f] \u672a\u627e\u5230 armamentarium \u771f\u5b9e\u4f24\u5bb3\u7c7b\u578b -> \u9000\u56de\u9b54\u6cd5\u4f24\u5bb3");
            }
        }
        if (TRUE_DMG_KEY == null) {
            return sl.m_269111_().m_269425_();
        }
        Holder.Reference holder = sl.m_9598_().m_175515_(Registries.f_268580_).m_246971_(TRUE_DMG_KEY);
        return new DamageSource((Holder)holder, (Entity)this.boss, (Entity)this.boss);
    }

    private void countSkillAndMaybeExhaust() {
        ++this.skillCount;
        if (this.skillCount % 3 != 0) {
            return;
        }
        if (this.boss.isExhausted()) {
            return;
        }
        long now = this.boss.m_9236_().m_46467_();
        if (now - this.lastMisfire < 600L) {
            return;
        }
    }

    private void beamFx(ServerLevel sl) {
        double x = this.boss.m_20185_();
        double y = this.boss.m_20186_() + 1.7;
        double z = this.boss.m_20189_();
        Fx.particles((Level)sl, x + this.beamDx * 0.9, y + this.beamDy * 0.9, z + this.beamDz * 0.9, 3, (ParticleOptions)ParticleTypes.f_123810_, 0.14, 0.03);
        for (double d = 2.0; d <= 40.0; d += 2.5) {
            double px = x + this.beamDx * d;
            double py = y + this.beamDy * d;
            double pz = z + this.beamDz * d;
            Fx.particles((Level)sl, px, py, pz, 1, (ParticleOptions)ParticleTypes.f_175830_, 0.18, 0.01);
        }
        Fx.particles((Level)sl, x, y, z, 1, (ParticleOptions)ParticleTypes.f_123745_, 0.2, 0.01);
    }

    private boolean tickCurrentIntergrowth(ServerLevel sl) {
        if (this.boss.getPhase() < 2) {
            return false;
        }
        if (this.currentCd > 0 && --this.currentCd > 0) {
            return false;
        }
        if (this.lightningUsed < 2) {
            this.currentCd = 100;
            return false;
        }
        int n = Math.min(this.lightningUsed, 12);
        double bx = this.boss.m_20185_();
        double by = this.boss.m_20186_() + 1.8;
        double bz = this.boss.m_20189_();
        this.boss.playCastAnim(OrbType.LIGHTNING);
        List victims = sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(40.0));
        for (int i = 0; i < n; ++i) {
            double ox = bx + (this.boss.m_217043_().m_188500_() - 0.5) * 6.0;
            double oz = bz + (this.boss.m_217043_().m_188500_() - 0.5) * 6.0;
            LightningBolt bolt = (LightningBolt)EntityType.f_20465_.m_20615_((Level)sl);
            if (bolt != null) {
                bolt.m_6027_(ox, this.boss.m_20186_(), oz);
                bolt.m_20874_(true);
                sl.m_7967_((Entity)bolt);
            }
            for (ServerPlayer p : victims) {
                if (!p.m_6084_() || p.m_20275_(ox, this.boss.m_20186_(), oz) > 12.0) continue;
                p.f_19802_ = 0;
                p.m_6469_(sl.m_269111_().m_269548_(), BossScaling.dmg((LivingEntity)this.boss, 12.0f + (float)this.boss.getPhase() * 6.0f));
            }
            sl.m_6263_(null, ox, this.boss.m_20186_(), oz, SoundEvents.f_12090_, SoundSource.HOSTILE, 1.2f, 1.0f);
        }
        this.boss.playCastAnim(OrbType.LIGHTNING);
        sl.m_6263_(null, bx, by, bz, SoundEvents.f_12090_, SoundSource.HOSTILE, 2.2f, 0.9f);
        this.broadcast("\u00a7e\u3010\u7535\u6d41\u76f8\u751f\u3011\u00a77 \u6838\u5fc3\u628a\u7528\u6389\u7684 \u00a7e" + n + "\u00a77 \u9897\u7535\u7403\u5168\u90e8\u53ec\u56de! \u968f\u4e4b\u8fdb\u5165 \u00a7c5 \u79d2\u865a\u5f31\u00a77(\u56de\u8840 10%)");
        FTBossMod.LOGGER.info("[FTBoss][\u7535\u6d41\u76f8\u751f] \u4e8c\u9636\u6bb5\u65bd\u653e: \u5df2\u7528\u7535\u7403 {} \u9897 -> \u843d\u96f7 {} \u9053, \u968f\u540e\u865a\u5f31 {}t(\u56de\u8840 {}%)", new Object[]{this.lightningUsed, n, 100, 10});
        this.boss.enterExhausted(100, 0.1f);
        this.currentCd = 600;
        return true;
    }

    private ServerPlayer nearest(ServerLevel sl, double radius) {
        LivingEntity priority = BossAggro.highest((LivingEntity)this.boss);
        if (priority != null) {
            ServerPlayer player;
            return priority instanceof ServerPlayer && this.boss.m_20280_((Entity)(player = (ServerPlayer)priority)) <= radius * radius ? player : null;
        }
        ServerPlayer best = null;
        double bd = radius * radius;
        for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(radius))) {
            if (!BossTargets.valid((Player)p)) continue;
            double d = this.boss.m_20280_((Entity)p);
            if (!p.m_6084_() || !(d < bd)) continue;
            bd = d;
            best = p;
        }
        return best;
    }

    private void broadcast(String msg) {
        if (msg == null) {
            return;
        }
        if (!(msg.contains("\u5149\u675f") || msg.contains("\u865a\u5f31") || msg.contains("\u8fc7\u8f7d") || msg.contains("\u8870\u5f31"))) {
            return;
        }
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(48.0))) {
            p.m_5661_((Component)Component.m_237113_((String)msg), true);
        }
    }

    private record QueuedCast(OrbType type, int charge, UUID orbId) {
    }
}

