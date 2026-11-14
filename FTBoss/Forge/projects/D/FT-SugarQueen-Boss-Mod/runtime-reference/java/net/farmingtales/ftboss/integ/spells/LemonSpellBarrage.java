/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.CastType
 *  io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LemonSpellBarrage {
    private static final Map<ServerLevel, Map<UUID, Burst>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Burst>>();
    private static final String READY = "ftboss:manor_spell_ready";
    private static final int MAX_ACTIVE = 64;

    private LemonSpellBarrage() {
    }

    private static List<AbstractSpell> pool(EasterBossEntity boss) {
        LinkedHashMap spells = new LinkedHashMap();
        ManorSpellRules.pair(boss.getVariant()).forEach(kind -> {
            ManorSignatureSpell spell = ManorIronSpells.spell(kind);
            spells.put(spell.getSpellId(), spell);
        });
        boss.lemonSacrifice().spells().forEach(kind -> {
            BossSignatureSpell spell = BossIronSpells.spell(kind);
            spells.put(spell.getSpellId(), spell);
        });
        boolean queen = boss.lemonSacrifice().has("sugar_queen");
        boolean tea = boss.lemonSacrifice().has("tea_tree");
        if (queen || tea || boss.lemonSacrifice().has("wine_barrel")) {
            WizardSummon.spellPool().stream().filter(spell -> queen || tea || WizardSummon.isFire(spell)).forEach(spell -> spells.putIfAbsent(spell.getSpellId(), spell));
        }
        if (tea) {
            WizardSummon.spellPoolOf("OAKSKIN_SPELL", "ABSORPTION_SPELL").forEach(spell -> spells.putIfAbsent(spell.getSpellId(), spell));
        }
        return spells.values().stream().filter(AbstractSpell::isEnabled).toList();
    }

    private static String cooldownKey(AbstractSpell spell) {
        if (spell instanceof ManorSignatureSpell) {
            return "ftboss:manor_cd_" + spell.getSpellResource().m_135815_();
        }
        if (spell instanceof BossSignatureSpell) {
            return "ftboss:signature_ready:" + spell.getSpellResource().m_135815_();
        }
        return "ftboss:lemon_iron_cd:" + spell.getSpellId();
    }

    private static int rank(EasterBossEntity boss, AbstractSpell spell) {
        int desired = spell instanceof ManorSignatureSpell || spell instanceof BossSignatureSpell ? 1 : (boss.lemonSacrifice().has("tea_tree") ? 6 : (boss.lemonSacrifice().has("wine_barrel") ? 4 : 3)) + BossRage.tierOf((LivingEntity)boss);
        return Math.max(spell.getMinLevel(), Math.min(desired, spell.getMaxLevel()));
    }

    public static void tick(ServerLevel level, EasterBossEntity boss, LivingEntity target) {
        Burst burst;
        Map casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap());
        long now = level.m_46467_();
        if (now % 20L == 0L) {
            casts.entrySet().removeIf(entry -> level.m_8791_((UUID)entry.getKey()) == null);
        }
        if ((burst = (Burst)casts.get(boss.m_20148_())) != null) {
            long age = now - burst.started();
            if (!boss.lemonSacrifice().active() || !target.m_20148_().equals(burst.target()) || burst.inheritedUntil() != boss.lemonSacrifice().state().m_128454_("until") || burst.spells().size() > boss.lemonSacrifice().spellsPerCast() || age < 0L || age > (long)(burst.duration() + 20) || burst.spells().stream().anyMatch(cast -> !cast.spell().isEnabled() || cast.spell().shouldAIStopCasting(cast.rank(), (Mob)boss, target))) {
                LemonSpellBarrage.cancel(boss);
                return;
            }
            if (burst.lastTick() == now) {
                return;
            }
            casts.put(boss.m_20148_(), new Burst(burst.target(), burst.inheritedUntil(), burst.started(), burst.duration(), now, burst.spells()));
            for (SpellCast cast2 : burst.spells()) {
                try {
                    LemonSpellBarrage.aimed(boss, target, () -> {
                        if (age < (long)burst.duration()) {
                            cast2.magic().handleCastDuration();
                            cast2.spell().onServerCastTick((Level)level, cast2.rank(), (LivingEntity)boss, cast2.magic());
                            if (cast2.spell().getCastType() == CastType.CONTINUOUS && age % 10L == 0L) {
                                cast2.spell().onCast((Level)level, cast2.rank(), (LivingEntity)boss, CastSource.MOB, cast2.magic());
                            }
                        } else if (cast2.spell().getCastType() != CastType.CONTINUOUS && boss.m_6084_()) {
                            cast2.spell().onCast((Level)level, cast2.rank(), (LivingEntity)boss, CastSource.MOB, cast2.magic());
                        }
                    });
                }
                catch (RuntimeException failure) {
                    FTBossMod.LOGGER.warn("[FTBoss][Lemon] barrage spell failed {}: {}", (Object)cast2.spell().getSpellId(), (Object)failure.toString());
                    LemonSpellBarrage.cancel(boss);
                    return;
                }
            }
            if (age >= (long)burst.duration()) {
                LemonSpellBarrage.finish(level, boss, false);
            }
            return;
        }
        if (!boss.getPersistentData().m_128425_(READY, 99)) {
            boss.getPersistentData().m_128356_(READY, now + 100L + (long)Math.floorMod(boss.m_19879_(), 40));
            return;
        }
        if (now < boss.getPersistentData().m_128454_(READY) || casts.size() >= 64 || boss.m_20280_((Entity)target) > 576.0) {
            return;
        }
        List<AbstractSpell> available = LemonSpellBarrage.pool(boss);
        if (available.isEmpty()) {
            return;
        }
        ArrayList<SpellCast> selected = new ArrayList<SpellCast>();
        int next = Math.floorMod(boss.lemonSacrifice().state().m_128451_("barrage_next"), available.size());
        int duration = 1;
        for (int offset = 0; offset < available.size() && selected.size() < boss.lemonSacrifice().spellsPerCast(); ++offset) {
            int index = (next + offset) % available.size();
            AbstractSpell spell = available.get(index);
            if (now < boss.getPersistentData().m_128454_(LemonSpellBarrage.cooldownKey(spell)) || spell.getSpellId().startsWith("irons_spellbooks:") && !BossSpellPacing.tryLegacyCast((LivingEntity)boss, spell)) continue;
            int rank = LemonSpellBarrage.rank(boss, spell);
            MagicData magic = new MagicData(true);
            magic.setSyncedData(new SyncedSpellData((LivingEntity)boss));
            try {
                boolean[] allowed = new boolean[]{false};
                LemonSpellBarrage.aimed(boss, target, () -> {
                    allowed[0] = spell.checkPreCastConditions((Level)level, rank, (LivingEntity)boss, magic);
                });
                if (!allowed[0]) continue;
                int castTicks = spell.getEffectiveCastTime(rank, (LivingEntity)boss);
                if (!(spell instanceof ManorSignatureSpell) && spell.getCastType() == CastType.LONG) {
                    castTicks = boss.getChallengeDifficulty().acceleratedTicks(castTicks);
                }
                duration = Math.max(duration, Math.max(1, Math.min(200, castTicks)));
                selected.add(new SpellCast(spell, rank, magic));
                boss.lemonSacrifice().state().m_128405_("barrage_next", (index + 1) % available.size());
                continue;
            }
            catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] skipped incompatible spell {}: {}", (Object)spell.getSpellId(), (Object)failure.toString());
            }
        }
        if (selected.isEmpty()) {
            return;
        }
        Burst started = new Burst(target.m_20148_(), boss.lemonSacrifice().state().m_128454_("until"), now, duration, now, List.copyOf(selected));
        casts.put(boss.m_20148_(), started);
        for (SpellCast cast3 : selected) {
            try {
                cast3.magic().initiateCast(cast3.spell(), cast3.rank(), duration, CastSource.MOB, "mainhand");
                LemonSpellBarrage.aimed(boss, target, () -> cast3.spell().onServerPreCast((Level)level, cast3.rank(), (LivingEntity)boss, cast3.magic()));
                int cooldown = cast3.spell() instanceof BossSignatureSpell ? BossSpellPacing.signatureCooldown(cast3.spell().getSpellCooldown()) : Math.max(100, cast3.spell().getSpellCooldown());
                boss.getPersistentData().m_128356_(LemonSpellBarrage.cooldownKey(cast3.spell()), now + (long)cooldown);
            }
            catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] barrage preparation failed: {}", (Object)failure.toString());
                LemonSpellBarrage.cancel(boss);
                return;
            }
        }
        boss.getPersistentData().m_128473_("ftboss:manor_spell_until");
        boss.getPersistentData().m_128473_("ftboss:signature_cast");
        boss.getPersistentData().m_128356_(READY, now + (long)duration + (long)ManorBossCombatRules.spellInterval(boss.getVariant()));
        boss.playParallelSpell(duration);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void aimed(EasterBossEntity boss, LivingEntity target, Runnable action) {
        float yaw = boss.m_146908_();
        float pitch = boss.m_146909_();
        Vec3 direction = target.m_146892_().m_82546_(boss.m_146892_());
        boss.m_146922_((float)Math.toDegrees(Math.atan2(-direction.f_82479_, direction.f_82481_)));
        boss.m_146926_((float)(-Math.toDegrees(Math.atan2(direction.f_82480_, direction.m_165924_()))));
        try {
            boss.lemonSacrifice().runInherited(action);
        }
        finally {
            boss.m_146922_(yaw);
            boss.m_146926_(pitch);
        }
    }

    public static void cancel(EasterBossEntity boss) {
        Level level = boss.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            LemonSpellBarrage.finish(level2, boss, true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void finish(ServerLevel level, EasterBossEntity boss, boolean cancelled) {
        Burst burst;
        Map<UUID, Burst> casts = ACTIVE.get(level);
        Burst burst2 = burst = casts == null ? null : casts.remove(boss.m_20148_());
        if (burst == null) {
            return;
        }
        for (SpellCast cast : burst.spells()) {
            try {
                boss.lemonSacrifice().runInherited(() -> cast.spell().onServerCastComplete((Level)level, cast.rank(), (LivingEntity)boss, cast.magic(), cancelled));
            }
            catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] barrage completion failed: {}", (Object)failure.toString());
            }
            finally {
                cast.magic().resetCastingState();
            }
        }
        boss.finishParallelSpell();
    }

    static void unload(ServerLevel level) {
        Map<UUID, Burst> casts = ACTIVE.get(level);
        if (casts == null) {
            return;
        }
        for (UUID id : List.copyOf(casts.keySet())) {
            Entity entity = level.m_8791_(id);
            if (!(entity instanceof EasterBossEntity)) continue;
            EasterBossEntity boss = (EasterBossEntity)entity;
            LemonSpellBarrage.cancel(boss);
        }
        ACTIVE.remove(level);
    }

    static void clear() {
        ACTIVE.clear();
    }

    private record Burst(UUID target, long inheritedUntil, long started, int duration, long lastTick, List<SpellCast> spells) {
    }

    private record SpellCast(AbstractSpell spell, int rank, MagicData magic) {
    }
}

