package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ManorSparrowCasting {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<>();
    private static final double SUPPORT_RANGE = 24;
    private static final int MAX_ACTIVE = 64;

    private ManorSparrowCasting() {}

    public static boolean delegates(Kind kind) {
        return switch (kind) {
            case EFFIGY_VOLLEY, CANDLE_PRISON, BLOOD_VERDICT, CITRUS_NOVA,
                    VOID_ECHO, SERAPH_DESCENT, WHITEOUT_EDICT -> true;
            default -> false;
        };
    }

    private static boolean ready(EasterBossEntity boss) {
        return boss.isAlive() && !boss.isRemoved() && !boss.isInvulnerable()
                && !boss.isMini() && !boss.isGiant() && ManorSpellCombat.eligible(boss) && !SparrowBorrowedSpells.busy(boss);
    }

    private static boolean sameFight(EasterBossEntity boss, EasterBossEntity sparrow) {
        return Objects.equals(ManorFightMembers.fightId(boss), ManorFightMembers.fightId(sparrow))
                && Objects.equals(boss.getManorSummonerId(), sparrow.getManorSummonerId());
    }

    public static int start(ServerLevel level, EasterBossEntity boss, LivingEntity target, Kind kind) {
        if (!delegates(kind) || kind.variant != boss.getVariant() || !ManorBossCombatRules.prefersMelee(boss.getVariant())
                || !ready(boss) || target == null || !BossSpellEffects.canTarget(boss, target)
                || target.level() != level || boss.lemonSacrifice().active()) return 0;
        Map<UUID, Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        casts.entrySet().removeIf(entry -> level.getEntity(entry.getKey()) == null);
        if (casts.size() >= MAX_ACTIVE || casts.values().stream().anyMatch(cast -> cast.owner().equals(boss.getUUID()))) return 0;
        EasterBossEntity sparrow = level.getEntitiesOfClass(EasterBossEntity.class,
                boss.getBoundingBox().inflate(SUPPORT_RANGE), candidate -> candidate.getVariant() == EasterBossEntity.SPARROW_VARIANT
                        && ready(candidate) && sameFight(boss, candidate) && !candidate.isCastingLike()
                        && !candidate.isPerformingSkill() && !casts.containsKey(candidate.getUUID())
                        && candidate.distanceToSqr(boss) <= SUPPORT_RANGE * SUPPORT_RANGE
                        && candidate.distanceToSqr(target) <= ManorSpellRules.RANGE * ManorSpellRules.RANGE
                        && candidate.hasLineOfSight(target) && BossSpellEffects.canTarget(candidate, target))
                .stream().min(java.util.Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
        if (sparrow == null) return 0;
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        if (!spell.isEnabled()) return 0;
        int rank = Math.max(spell.getMinLevel(), Math.min(2, spell.getMaxLevel()));
        int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, sparrow)));
        MagicData magic = new MagicData(true);
        magic.setSyncedData(new SyncedSpellData(sparrow));
        if (!spell.checkPreCastConditions(level, rank, sparrow, magic)) return 0;
        Vec3 point = target.position().add(0, 0.5, 0);
        sparrow.getLookControl().setLookAt(target, 60, 60);
        magic.initiateCast(spell, rank, duration, CastSource.MOB, "mainhand");
        spell.onServerPreCast(level, rank, sparrow, magic);
        magic.setAdditionalCastData(new BossSignatureSpell.Aim(point));
        casts.put(sparrow.getUUID(), new Cast(boss.getUUID(), target.getUUID(), kind, level.getGameTime(), duration, rank, magic));
        sparrow.playParallelSpell(duration);
        ManorSpellEffects.warning(level, sparrow, kind, point);
        FTBossMod.LOGGER.info("[FTBoss][Manor] {} 将 {} 交给麻雀施放", boss.getName().getString(), kind.id);
        return duration;
    }

    public static UUID ownerOf(LivingEntity caster, Kind kind) {
        if (!(caster.level() instanceof ServerLevel level)) return null;
        Map<UUID, Cast> casts = ACTIVE.get(level);
        Cast cast = casts == null ? null : casts.get(caster.getUUID());
        return cast != null && cast.kind() == kind ? cast.owner() : SparrowBorrowedSpells.ownerOf(caster, kind);
    }

    public static boolean isCastingFor(ServerLevel level, EasterBossEntity boss) {
        Map<UUID, Cast> casts = ACTIVE.get(level);
        return casts != null && casts.values().stream().anyMatch(cast -> cast.owner().equals(boss.getUUID()));
    }

    public static boolean validField(ServerLevel level, EasterBossEntity sparrow, Kind kind, UUID owner) {
        if (SparrowBorrowedSpells.validField(level, sparrow, kind, owner)) return true;
        return owner != null && delegates(kind) && sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT
                && ready(sparrow) && level.getEntity(owner) instanceof EasterBossEntity boss && ready(boss)
                && boss.getVariant() == kind.variant && !boss.lemonSacrifice().active() && sameFight(boss, sparrow)
                && boss.distanceToSqr(sparrow) <= 48 * 48;
    }

    public static void tick(ServerLevel level, EasterBossEntity sparrow) {
        Map<UUID, Cast> casts = ACTIVE.get(level);
        Cast cast = casts == null ? null : casts.get(sparrow.getUUID());
        if (cast == null) return;
        ManorSignatureSpell spell = ManorIronSpells.spell(cast.kind());
        long age = level.getGameTime() - cast.started();
        LivingEntity target = level.getEntity(cast.target()) instanceof LivingEntity living ? living : null;
        EasterBossEntity owner = level.getEntity(cast.owner()) instanceof EasterBossEntity boss ? boss : null;
        if (!validField(level, sparrow, cast.kind(), cast.owner()) || owner == null || owner.getTarget() != target
                || target == null || !BossSpellEffects.canTarget(sparrow, target) || !BossSpellEffects.canTarget(owner, target)
                || sparrow.distanceToSqr(target) > 32 * 32 || !sparrow.hasLineOfSight(target)
                || !spell.isEnabled() || age < 0 || age > cast.duration() + 20) {
            finish(level, sparrow, casts, cast, true);
            return;
        }
        sparrow.getLookControl().setLookAt(target, 60, 60);
        if (age < cast.duration()) {
            cast.magic().handleCastDuration();
            spell.onServerCastTick(level, cast.rank(), sparrow, cast.magic());
            return;
        }
        try {
            spell.onCast(level, cast.rank(), sparrow, CastSource.MOB, cast.magic());
        } finally {
            finish(level, sparrow, casts, cast, false);
        }
    }

    private static void finish(ServerLevel level, EasterBossEntity sparrow, Map<UUID, Cast> casts, Cast cast, boolean cancelled) {
        casts.remove(sparrow.getUUID());
        try {
            ManorIronSpells.spell(cast.kind()).onServerCastComplete(level, cast.rank(), sparrow, cast.magic(), cancelled);
        } finally {
            sparrow.finishParallelSpell();
            if (casts.isEmpty()) ACTIVE.remove(level);
        }
    }

    public static void unload(ServerLevel level) {
        Map<UUID, Cast> casts = ACTIVE.remove(level);
        if (casts == null) return;
        for (UUID identifier : java.util.List.copyOf(casts.keySet())) {
            if (level.getEntity(identifier) instanceof EasterBossEntity sparrow) finish(level, sparrow, casts, casts.get(identifier), true);
        }
    }

    public static void clear() { ACTIVE.clear(); }

    private record Cast(UUID owner, UUID target, Kind kind, long started, int duration, int rank, MagicData magic) {}
}
