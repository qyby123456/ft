/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastSync;
import net.farmingtales.ftboss.animation.zicaicast.ZicaiCastSync;
import net.farmingtales.ftboss.body.BodySignals;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.LemonSpellBarrage;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSparrowCasting;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.integ.spells.YouziCloneSpellProfile;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.farmingtales.ftboss.xueli_multiplayer.CadenceServer;
import net.farmingtales.ftboss.zheergenvoidbody.VoidSignals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorSpellCombat {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Cast>>();
    private static final String READY = "ftboss:manor_spell_ready";
    private static final String NEXT = "ftboss:manor_spell_next";
    private static final int MAX_ACTIVE = 64;

    private ManorSpellCombat() {
    }

    private static int nextInterval(int variant) {
        return (int)Math.round((double)ManorBossCombatRules.spellInterval(variant) * ManorBossCombatRules.spellIntervalMultiplier(variant));
    }

    public static boolean eligible(EasterBossEntity boss) {
        return boss.getVariant() != 11 && ManorSpellRules.eligible(boss.isMini() && !YouziCloneSpellProfile.isClone((LivingEntity)boss), boss.isGiant(), boss.m_21525_(), ManorFight.isFinaleMember((LivingEntity)boss));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean valid(EasterBossEntity boss, LivingEntity target) {
        if (!ManorSpellCombat.eligible(boss)) return false;
        if (!boss.m_6084_()) return false;
        if (boss.m_20147_()) return false;
        if (target == null) return false;
        if (target.m_9236_() != boss.m_9236_()) return false;
        if (!BossSpellEffects.canTarget((LivingEntity)boss, target)) return false;
        double d = boss.m_20280_((Entity)target);
        int n = YouziCloneSpellProfile.isClone((LivingEntity)boss) ? 256 : 1024;
        if (!(d <= (double)n)) return false;
        if (!boss.m_142582_((Entity)target)) return false;
        return true;
    }

    public static boolean replace(EasterBossEntity boss, LivingEntity target, String id) {
        ServerLevel level;
        block9: {
            block8: {
                Level level2 = boss.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block8;
                level = (ServerLevel)level2;
                if (ManorSpellCombat.eligible(boss)) break block9;
            }
            return false;
        }
        ManorSpellRules.Kind kind = ManorSpellRules.pair(boss.getVariant()).stream().filter(candidate -> candidate.id.equals(id)).findFirst().orElse(null);
        if (kind == null || !ManorIronSpells.spell(kind).isEnabled()) {
            return false;
        }
        if (!ManorSpellCombat.valid(boss, target)) {
            return true;
        }
        if (boss.lemonSacrifice().active()) {
            Map<UUID, Cast> previous = ACTIVE.get(level);
            if (previous != null) {
                ManorSpellCombat.finish(level, boss, previous, true);
            }
            LemonSpellBarrage.tick(level, boss, target);
            return true;
        }
        Map casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap());
        long now = level.m_46467_();
        if (casts.containsKey(boss.m_20148_()) || ManorSparrowCasting.isCastingFor(level, boss) || YouziCloneSpellProfile.isClone((LivingEntity)boss) && now < boss.getPersistentData().m_128454_(READY) || now < boss.getPersistentData().m_128454_("ftboss:manor_cd_" + kind.id)) {
            return true;
        }
        ManorSpellCombat.start(level, boss, target, kind, casts, now);
        return true;
    }

    private static boolean start(ServerLevel level, EasterBossEntity boss, LivingEntity target, ManorSpellRules.Kind kind, Map<UUID, Cast> casts, long now) {
        int delegated;
        ManorSpellRules.Kind secondary;
        if (casts.size() >= 64) {
            return false;
        }
        List<ManorSpellRules.Kind> enabled = ManorSpellRules.pair(boss.getVariant()).stream().filter(candidate -> ManorIronSpells.spell(candidate).isEnabled()).filter(candidate -> boss.getVariant() != 9 || candidate.support()).toList();
        boolean clone = YouziCloneSpellProfile.isClone((LivingEntity)boss);
        int spellCount = clone ? 1 : ManorBossCombatRules.spellsPerCast(boss.getVariant());
        ManorSpellRules.Kind kind2 = secondary = spellCount < 2 ? null : (ManorSpellRules.Kind)enabled.stream().filter(candidate -> candidate != kind && now >= boss.getPersistentData().m_128454_("ftboss:manor_cd_" + candidate.id)).findFirst().orElse(null);
        if (spellCount > 1 && secondary == null && enabled.size() >= spellCount) {
            return false;
        }
        int n = delegated = spellCount == 1 ? ManorSparrowCasting.start(level, boss, target, kind) : 0;
        if (delegated > 0) {
            boss.getPersistentData().m_128356_("ftboss:manor_cd_" + kind.id, CadenceServer.cooldown(now + (long)Math.max(100, ManorIronSpells.spell(kind).getSpellCooldown()), boss, now));
            boss.getPersistentData().m_128356_(READY, CadenceServer.ready(now + (long)delegated + (long)ManorBossCombatRules.spellInterval(boss.getVariant()), boss, now, delegated));
            return true;
        }
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        int rank = Math.max(spell.getMinLevel(), Math.min(2, spell.getMaxLevel()));
        int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, (LivingEntity)boss)));
        MagicData magic = new MagicData(true);
        magic.setSyncedData(new SyncedSpellData((LivingEntity)boss));
        if (!spell.checkPreCastConditions((Level)level, rank, (LivingEntity)boss, magic)) {
            return false;
        }
        MagicData secondaryMagic = null;
        if (secondary != null) {
            ManorSignatureSpell secondSpell = ManorIronSpells.spell(secondary);
            secondaryMagic = new MagicData(true);
            secondaryMagic.setSyncedData(new SyncedSpellData((LivingEntity)boss));
            if (!secondSpell.checkPreCastConditions((Level)level, rank, (LivingEntity)boss, secondaryMagic)) {
                return false;
            }
            duration = Math.max(duration, Math.max(1, Math.min(200, secondSpell.getEffectiveCastTime(rank, (LivingEntity)boss))));
            secondaryMagic.initiateCast((AbstractSpell)secondSpell, rank, duration, CastSource.MOB, "offhand");
            secondSpell.onServerPreCast((Level)level, rank, (LivingEntity)boss, secondaryMagic);
            secondaryMagic.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(secondary.support() ? boss.m_20182_() : BossSpellEffects.aim((LivingEntity)boss)));
            ManorSpellEffects.warning(level, (LivingEntity)boss, secondary, BossSpellEffects.aim((LivingEntity)boss));
        }
        magic.initiateCast((AbstractSpell)spell, rank, duration, CastSource.MOB, "mainhand");
        spell.onServerPreCast((Level)level, rank, (LivingEntity)boss, magic);
        Vec3 center = kind.support() ? boss.m_20182_() : BossSpellEffects.aim((LivingEntity)boss);
        magic.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(center));
        casts.put(boss.m_20148_(), new Cast(kind, target.m_20148_(), center, now, duration, rank, magic, secondary, secondaryMagic));
        CompoundTag data = boss.getPersistentData();
        data.m_128473_("ftboss:manor_spell_until");
        data.m_128356_("ftboss:manor_cd_" + kind.id, CadenceServer.cooldown(now + (long)YouziCloneSpellProfile.cooldown((LivingEntity)boss, spell.getSpellCooldown()), boss, now));
        if (secondary != null) {
            data.m_128356_("ftboss:manor_cd_" + secondary.id, CadenceServer.cooldown(now + (long)Math.max(100, ManorIronSpells.spell(secondary).getSpellCooldown()), boss, now));
        }
        data.m_128356_(READY, CadenceServer.ready(now + (long)duration + (long)(clone ? 200 : ManorSpellCombat.nextInterval(boss.getVariant())), boss, now, duration));
        VoidSignals.begin(boss, duration, kind, secondary);
        CastServer.iron(boss, kind, secondary, duration);
        BodySignals.iron(boss, kind, duration);
        ZicaiCastSync.start(boss, kind, duration);
        ManorSpellEffects.warning(level, (LivingEntity)boss, kind, center);
        QingyiCastSync.start(boss, kind, duration);
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        EasterBossEntity boss;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof EasterBossEntity) || !((livingEntity = (boss = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)livingEntity;
        ManorSparrowCasting.tick(level, boss);
        Map casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap());
        long now = level.m_46467_();
        if (now % 20L == 0L) {
            casts.entrySet().removeIf(entry -> level.m_8791_((UUID)entry.getKey()) == null);
        }
        LivingEntity target = boss.m_5448_();
        Cast cast = (Cast)casts.get(boss.m_20148_());
        if (!ManorSpellCombat.valid(boss, target)) {
            ManorSpellCombat.finish(level, boss, casts, true);
            LemonSpellBarrage.cancel(boss);
            return;
        }
        if (boss.lemonSacrifice().active()) {
            ManorSpellCombat.finish(level, boss, casts, true);
            LemonSpellBarrage.tick(level, boss, target);
            return;
        }
        LemonSpellBarrage.cancel(boss);
        if (ManorSparrowCasting.isCastingFor(level, boss)) {
            return;
        }
        if (cast != null) {
            ManorSignatureSpell spell = ManorIronSpells.spell(cast.kind());
            long age = now - cast.started();
            if (!target.m_20148_().equals(cast.target()) || cast.kind().variant != boss.getVariant() || !spell.isEnabled() || cast.secondary() != null && !ManorIronSpells.spell(cast.secondary()).isEnabled() || age < 0L || age > (long)(cast.duration() + 20) || spell.shouldAIStopCasting(cast.rank(), (Mob)boss, target)) {
                ManorSpellCombat.finish(level, boss, casts, true);
                return;
            }
            if (age < (long)cast.duration()) {
                cast.magic().handleCastDuration();
                spell.onServerCastTick((Level)level, cast.rank(), (LivingEntity)boss, cast.magic());
                if (cast.secondary() != null) {
                    cast.secondaryMagic().handleCastDuration();
                    ManorIronSpells.spell(cast.secondary()).onServerCastTick((Level)level, cast.rank(), (LivingEntity)boss, cast.secondaryMagic());
                }
                return;
            }
            try {
                VoidSignals.cast(spell, (Level)level, cast.rank(), (LivingEntity)boss, CastSource.MOB, cast.magic());
                QingyiCastSync.release(boss);
                ZicaiCastSync.release(boss);
                if (cast.secondary() != null && boss.m_6084_()) {
                    VoidSignals.cast(ManorIronSpells.spell(cast.secondary()), (Level)level, cast.rank(), (LivingEntity)boss, CastSource.MOB, cast.secondaryMagic());
                }
            }
            finally {
                ManorSpellCombat.finish(level, boss, casts, false);
            }
            return;
        }
        boss.getPersistentData().m_128473_("ftboss:manor_spell_until");
        CompoundTag data = boss.getPersistentData();
        if (!data.m_128425_(READY, 99)) {
            boolean clone = YouziCloneSpellProfile.isClone((LivingEntity)boss);
            data.m_128356_(READY, now + (long)(clone ? 40 : 100) + (long)Math.floorMod(boss.m_19879_(), 40));
            if (clone) {
                data.m_128405_(NEXT, Math.floorMod(boss.m_19879_(), ManorSpellRules.pair(boss.getVariant()).size()));
            }
            return;
        }
        if (now < data.m_128454_(READY) || boss.m_20280_((Entity)target) > 576.0) {
            return;
        }
        List<ManorSpellRules.Kind> pair = ManorSpellRules.pair(boss.getVariant());
        for (int offset = 0; offset < pair.size(); ++offset) {
            int index = Math.floorMod(data.m_128451_(NEXT) + offset, pair.size());
            ManorSpellRules.Kind kind = pair.get(index);
            if (boss.getVariant() == 9 && !kind.support() || !ManorIronSpells.spell(kind).isEnabled() || now < data.m_128454_("ftboss:manor_cd_" + kind.id)) continue;
            if (!ManorSpellCombat.start(level, boss, target, kind, casts, now)) break;
            data.m_128405_(NEXT, (index + 1) % pair.size());
            break;
        }
    }

    private static void finish(ServerLevel level, EasterBossEntity boss, Map<UUID, Cast> casts, boolean cancelled) {
        Cast cast = casts.remove(boss.m_20148_());
        boss.getPersistentData().m_128473_("ftboss:manor_spell_until");
        if (cast == null) {
            return;
        }
        ManorIronSpells.spell(cast.kind()).onServerCastComplete((Level)level, cast.rank(), (LivingEntity)boss, cast.magic(), cancelled);
        if (cast.secondary() != null) {
            ManorIronSpells.spell(cast.secondary()).onServerCastComplete((Level)level, cast.rank(), (LivingEntity)boss, cast.secondaryMagic(), cancelled);
        }
        VoidSignals.complete(boss, cancelled);
        QingyiCastSync.finish(boss, cancelled);
        CastServer.finishIron(boss, cancelled);
        ZicaiCastSync.finish(boss, cancelled);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        ManorSparrowCasting.unload(level);
        LemonSpellBarrage.unload(level);
        Map<UUID, Cast> casts = ACTIVE.remove(level);
        if (casts == null) {
            return;
        }
        for (UUID id : List.copyOf(casts.keySet())) {
            Entity entity = level.m_8791_(id);
            if (!(entity instanceof EasterBossEntity)) continue;
            EasterBossEntity boss = (EasterBossEntity)entity;
            ManorSpellCombat.finish(level, boss, casts, true);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ManorSparrowCasting.clear();
        ACTIVE.clear();
        LemonSpellBarrage.clear();
    }

    private record Cast(ManorSpellRules.Kind kind, UUID target, Vec3 center, long started, int duration, int rank, MagicData magic, ManorSpellRules.Kind secondary, MagicData secondaryMagic) {
    }
}

