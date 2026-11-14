package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorSpellCombat {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<>();
    private static final String READY = "ftboss:manor_spell_ready";
    private static final String NEXT = "ftboss:manor_spell_next";
    private static final int MAX_ACTIVE = 64;

    private ManorSpellCombat() {}

    /**
     * 两位主动施法之间的冷却(tick)。
     *
     * <p>★ v178: 乘以 {@link ManorBossCombatRules#spellIntervalMultiplier(int)} ——
     * 功底(variant 3)按用户要求「减少铁魔法施法, 多用技能」把间隔拉到 1.8 倍
     * (约 16 秒 → 29 秒一轮); 其余变体倍数 1.0, 行为与改动前完全一致。
     */
    private static int nextInterval(int variant) {
        return (int) Math.round(ManorBossCombatRules.spellInterval(variant)
                * ManorBossCombatRules.spellIntervalMultiplier(variant));
    }

    public static boolean eligible(EasterBossEntity boss) {
        return boss.getVariant() != EasterBossEntity.QIZAI_VARIANT && ManorSpellRules.eligible(boss.isMini() && !YouziCloneSpellProfile.isClone(boss),
                boss.isGiant(), boss.isNoAi(), ManorFight.isFinaleMember(boss));
    }

    private static boolean valid(EasterBossEntity boss, LivingEntity target) {
        return eligible(boss) && boss.isAlive() && !boss.isInvulnerable() && target != null
                && target.level() == boss.level() && BossSpellEffects.canTarget(boss, target)
                && boss.distanceToSqr(target) <= (YouziCloneSpellProfile.isClone(boss)
                        ? YouziCloneSpellProfile.RANGE * YouziCloneSpellProfile.RANGE : 32 * 32)
                && boss.hasLineOfSight(target);
    }

    public static boolean replace(EasterBossEntity boss, LivingEntity target, String id) {
        if (!(boss.level() instanceof ServerLevel level) || !eligible(boss)) return false;
        Kind kind = ManorSpellRules.pair(boss.getVariant()).stream().filter(candidate -> candidate.id.equals(id)).findFirst().orElse(null);
        if (kind == null || !ManorIronSpells.spell(kind).isEnabled()) return false;
        if (!valid(boss, target)) return true;
        if (boss.lemonSacrifice().active()) {
            Map<UUID, Cast> previous = ACTIVE.get(level);
            if (previous != null) finish(level, boss, previous, true);
            LemonSpellBarrage.tick(level, boss, target);
            return true;
        }
        Map<UUID, Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        long now = level.getGameTime();
        if (casts.containsKey(boss.getUUID()) || ManorSparrowCasting.isCastingFor(level, boss)
                || YouziCloneSpellProfile.isClone(boss) && now < boss.getPersistentData().getLong(READY)
                || now < boss.getPersistentData().getLong("ftboss:manor_cd_" + kind.id)) return true;
        start(level, boss, target, kind, casts, now);
        return true;
    }

    private static boolean start(ServerLevel level, EasterBossEntity boss, LivingEntity target, Kind kind,
                                 Map<UUID, Cast> casts, long now) {
        if (casts.size() >= MAX_ACTIVE) return false;
        List<Kind> enabled = ManorSpellRules.pair(boss.getVariant()).stream()
                .filter(candidate -> ManorIronSpells.spell(candidate).isEnabled())
                .filter(candidate -> boss.getVariant() != EasterBossEntity.SPARROW_VARIANT || candidate.support()).toList();
        boolean clone = YouziCloneSpellProfile.isClone(boss);
        int spellCount = clone ? 1 : ManorBossCombatRules.spellsPerCast(boss.getVariant());
        Kind secondary = spellCount < 2 ? null : enabled.stream().filter(candidate -> candidate != kind
                && now >= boss.getPersistentData().getLong("ftboss:manor_cd_" + candidate.id)).findFirst().orElse(null);
        if (spellCount > 1 && secondary == null && enabled.size() >= spellCount) return false;
        int delegated = spellCount == 1 ? ManorSparrowCasting.start(level, boss, target, kind) : 0;
        if (delegated > 0) {
            boss.getPersistentData().putLong("ftboss:manor_cd_" + kind.id,
                    now + Math.max(100, ManorIronSpells.spell(kind).getSpellCooldown()));
            boss.getPersistentData().putLong(READY, now + delegated + ManorBossCombatRules.spellInterval(boss.getVariant()));
            return true;
        }
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        int rank = Math.max(spell.getMinLevel(), Math.min(2, spell.getMaxLevel()));
        int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, boss)));
        MagicData magic = new MagicData(true);
        magic.setSyncedData(new SyncedSpellData(boss));
        if (!spell.checkPreCastConditions(level, rank, boss, magic)) return false;
        MagicData secondaryMagic = null;
        if (secondary != null) {
            ManorSignatureSpell secondSpell = ManorIronSpells.spell(secondary);
            secondaryMagic = new MagicData(true);
            secondaryMagic.setSyncedData(new SyncedSpellData(boss));
            if (!secondSpell.checkPreCastConditions(level, rank, boss, secondaryMagic)) return false;
            duration = Math.max(duration, Math.max(1, Math.min(200, secondSpell.getEffectiveCastTime(rank, boss))));
            secondaryMagic.initiateCast(secondSpell, rank, duration, CastSource.MOB, "offhand");
            secondSpell.onServerPreCast(level, rank, boss, secondaryMagic);
            secondaryMagic.setAdditionalCastData(new BossSignatureSpell.Aim(
                    secondary.support() ? boss.position() : BossSpellEffects.aim(boss)));
            ManorSpellEffects.warning(level, boss, secondary, BossSpellEffects.aim(boss));
        }
        magic.initiateCast(spell, rank, duration, CastSource.MOB, "mainhand");
        spell.onServerPreCast(level, rank, boss, magic);
        Vec3 center = kind.support() ? boss.position() : BossSpellEffects.aim(boss);
        magic.setAdditionalCastData(new BossSignatureSpell.Aim(center));
        casts.put(boss.getUUID(), new Cast(kind, target.getUUID(), center, now, duration, rank, magic, secondary, secondaryMagic));
        CompoundTag data = boss.getPersistentData();
        data.remove(ManorSpellRules.LOCK_KEY);
        data.putLong("ftboss:manor_cd_" + kind.id, now + YouziCloneSpellProfile.cooldown(boss, spell.getSpellCooldown()));
        if (secondary != null) data.putLong("ftboss:manor_cd_" + secondary.id,
                now + Math.max(100, ManorIronSpells.spell(secondary).getSpellCooldown()));
        data.putLong(READY, now + duration + (clone ? YouziCloneSpellProfile.CAST_INTERVAL
                : nextInterval(boss.getVariant())));
        boss.playParallelSpell(duration);
        ManorSpellEffects.warning(level, boss, kind, center);
        return true;
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)) return;
        ManorSparrowCasting.tick(level, boss);
        Map<UUID, Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        long now = level.getGameTime();
        if (now % 20 == 0) casts.entrySet().removeIf(entry -> level.getEntity(entry.getKey()) == null);
        LivingEntity target = boss.getTarget();
        Cast cast = casts.get(boss.getUUID());
        if (!valid(boss, target)) {
            finish(level, boss, casts, true);
            LemonSpellBarrage.cancel(boss);
            return;
        }
        if (boss.lemonSacrifice().active()) {
            finish(level, boss, casts, true);
            LemonSpellBarrage.tick(level, boss, target);
            return;
        }
        LemonSpellBarrage.cancel(boss);
        if (ManorSparrowCasting.isCastingFor(level, boss)) return;
        if (cast != null) {
            ManorSignatureSpell spell = ManorIronSpells.spell(cast.kind());
            long age = now - cast.started();
            if (!target.getUUID().equals(cast.target()) || cast.kind().variant != boss.getVariant() || !spell.isEnabled()
                    || cast.secondary() != null && !ManorIronSpells.spell(cast.secondary()).isEnabled()
                    || age < 0 || age > cast.duration() + 20 || spell.shouldAIStopCasting(cast.rank(), boss, target)) {
                finish(level, boss, casts, true);
                return;
            }
            if (age < cast.duration()) {
                cast.magic().handleCastDuration();
                spell.onServerCastTick(level, cast.rank(), boss, cast.magic());
                if (cast.secondary() != null) {
                    cast.secondaryMagic().handleCastDuration();
                    ManorIronSpells.spell(cast.secondary()).onServerCastTick(level, cast.rank(), boss, cast.secondaryMagic());
                }
                return;
            }
            try {
                spell.onCast(level, cast.rank(), boss, CastSource.MOB, cast.magic());
                if (cast.secondary() != null && boss.isAlive()) ManorIronSpells.spell(cast.secondary())
                        .onCast(level, cast.rank(), boss, CastSource.MOB, cast.secondaryMagic());
            } finally {
                finish(level, boss, casts, false);
            }
            return;
        }
        boss.getPersistentData().remove(ManorSpellRules.LOCK_KEY);
        CompoundTag data = boss.getPersistentData();
        if (!data.contains(READY, 99)) {
            boolean clone = YouziCloneSpellProfile.isClone(boss);
            data.putLong(READY, now + (clone ? YouziCloneSpellProfile.INITIAL_DELAY : 100) + Math.floorMod(boss.getId(), 40));
            if (clone) data.putInt(NEXT, Math.floorMod(boss.getId(), ManorSpellRules.pair(boss.getVariant()).size()));
            return;
        }
        if (now < data.getLong(READY)
                || boss.distanceToSqr(target) > ManorSpellRules.RANGE * ManorSpellRules.RANGE) return;
        List<Kind> pair = ManorSpellRules.pair(boss.getVariant());
        for (int offset = 0; offset < pair.size(); offset++) {
            int index = Math.floorMod(data.getInt(NEXT) + offset, pair.size());
            Kind kind = pair.get(index);
            if (boss.getVariant() == EasterBossEntity.SPARROW_VARIANT && !kind.support()) continue;
            if (!ManorIronSpells.spell(kind).isEnabled() || now < data.getLong("ftboss:manor_cd_" + kind.id)) continue;
            if (start(level, boss, target, kind, casts, now)) data.putInt(NEXT, (index + 1) % pair.size());
            break;
        }
    }

    private static void finish(ServerLevel level, EasterBossEntity boss, Map<UUID, Cast> casts, boolean cancelled) {
        Cast cast = casts.remove(boss.getUUID());
        boss.getPersistentData().remove(ManorSpellRules.LOCK_KEY);
        if (cast == null) return;
        ManorIronSpells.spell(cast.kind()).onServerCastComplete(level, cast.rank(), boss, cast.magic(), cancelled);
        if (cast.secondary() != null) ManorIronSpells.spell(cast.secondary())
                .onServerCastComplete(level, cast.rank(), boss, cast.secondaryMagic(), cancelled);
        boss.finishParallelSpell();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ManorSparrowCasting.unload(level);
        LemonSpellBarrage.unload(level);
        Map<UUID, Cast> casts = ACTIVE.remove(level);
        if (casts == null) return;
        for (UUID id : List.copyOf(casts.keySet())) {
            if (level.getEntity(id) instanceof EasterBossEntity boss) finish(level, boss, casts, true);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ManorSparrowCasting.clear();
        ACTIVE.clear();
        LemonSpellBarrage.clear();
    }

    private record Cast(Kind kind, UUID target, Vec3 center, long started, int duration, int rank, MagicData magic,
                        Kind secondary, MagicData secondaryMagic) {}
}
