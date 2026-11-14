/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellCombat;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.integ.spells.SparrowBorrowedSpells;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ManorSparrowCasting {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Cast>>();
    private static final double SUPPORT_RANGE = 24.0;
    private static final int MAX_ACTIVE = 64;

    private ManorSparrowCasting() {
    }

    public static boolean delegates(ManorSpellRules.Kind kind) {
        return switch (kind) {
            case ManorSpellRules.Kind.EFFIGY_VOLLEY, ManorSpellRules.Kind.CANDLE_PRISON, ManorSpellRules.Kind.BLOOD_VERDICT, ManorSpellRules.Kind.CITRUS_NOVA, ManorSpellRules.Kind.VOID_ECHO, ManorSpellRules.Kind.SERAPH_DESCENT, ManorSpellRules.Kind.WHITEOUT_EDICT -> true;
            default -> false;
        };
    }

    private static boolean ready(EasterBossEntity easterBossEntity) {
        EasterBossEntity boss;
        if (SparrowBorrowedSpells.busy((LivingEntity)easterBossEntity)) {
            return false;
        }
        return boss.m_6084_() && !boss.m_213877_() && !boss.m_20147_() && !boss.isMini() && !boss.isGiant() && ManorSpellCombat.eligible(boss);
    }

    private static boolean sameFight(EasterBossEntity boss, EasterBossEntity sparrow) {
        return Objects.equals(ManorFightMembers.fightId((Entity)boss), ManorFightMembers.fightId((Entity)sparrow)) && Objects.equals(boss.getManorSummonerId(), sparrow.getManorSummonerId());
    }

    public static int start(ServerLevel level, EasterBossEntity boss, LivingEntity target, ManorSpellRules.Kind kind) {
        if (!(ManorSparrowCasting.delegates(kind) && kind.variant == boss.getVariant() && ManorBossCombatRules.prefersMelee(boss.getVariant()) && ManorSparrowCasting.ready(boss) && target != null && BossSpellEffects.canTarget((LivingEntity)boss, target) && target.m_9236_() == level && !boss.lemonSacrifice().active())) {
            return 0;
        }
        Map casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap());
        casts.entrySet().removeIf(entry -> level.m_8791_((UUID)entry.getKey()) == null);
        if (casts.size() >= 64 || casts.values().stream().anyMatch(cast -> cast.owner().equals(boss.m_20148_()))) {
            return 0;
        }
        EasterBossEntity sparrow = level.m_6443_(EasterBossEntity.class, boss.m_20191_().m_82400_(24.0), candidate -> candidate.getVariant() == 9 && ManorSparrowCasting.ready(candidate) && ManorSparrowCasting.sameFight(boss, candidate) && !candidate.isCastingLike() && !candidate.isPerformingSkill() && !casts.containsKey(candidate.m_20148_()) && candidate.m_20280_((Entity)boss) <= 576.0 && candidate.m_20280_((Entity)target) <= 576.0 && candidate.m_142582_((Entity)target) && BossSpellEffects.canTarget((LivingEntity)candidate, target)).stream().min(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)boss).m_20280_(arg_0))).orElse(null);
        if (sparrow == null) {
            return 0;
        }
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        if (!spell.isEnabled()) {
            return 0;
        }
        int rank = Math.max(spell.getMinLevel(), Math.min(2, spell.getMaxLevel()));
        int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, (LivingEntity)sparrow)));
        MagicData magic = new MagicData(true);
        magic.setSyncedData(new SyncedSpellData((LivingEntity)sparrow));
        if (!spell.checkPreCastConditions((Level)level, rank, (LivingEntity)sparrow, magic)) {
            return 0;
        }
        Vec3 point = target.m_20182_().m_82520_(0.0, 0.5, 0.0);
        sparrow.m_21563_().m_24960_((Entity)target, 60.0f, 60.0f);
        magic.initiateCast((AbstractSpell)spell, rank, duration, CastSource.MOB, "mainhand");
        spell.onServerPreCast((Level)level, rank, (LivingEntity)sparrow, magic);
        magic.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(point));
        casts.put(sparrow.m_20148_(), new Cast(boss.m_20148_(), target.m_20148_(), kind, level.m_46467_(), duration, rank, magic));
        sparrow.playParallelSpell(duration);
        ManorSpellEffects.warning(level, (LivingEntity)sparrow, kind, point);
        FTBossMod.LOGGER.info("[FTBoss][Manor] {} \u5c06 {} \u4ea4\u7ed9\u9ebb\u96c0\u65bd\u653e", (Object)boss.m_7755_().getString(), (Object)kind.id);
        return duration;
    }

    public static UUID ownerOf(LivingEntity caster, ManorSpellRules.Kind kind) {
        Level level = caster.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel level2 = (ServerLevel)level;
        Map<UUID, Cast> casts = ACTIVE.get(level2);
        Cast cast = casts == null ? null : casts.get(caster.m_20148_());
        return cast != null && cast.kind() == kind ? cast.owner() : SparrowBorrowedSpells.ownerOf(caster, kind);
    }

    public static boolean isCastingFor(ServerLevel level, EasterBossEntity boss) {
        Map<UUID, Cast> casts = ACTIVE.get(level);
        return casts != null && casts.values().stream().anyMatch(cast -> cast.owner().equals(boss.m_20148_()));
    }

    /*
     * WARNING - void declaration
     */
    public static boolean validField(ServerLevel serverLevel, EasterBossEntity easterBossEntity, ManorSpellRules.Kind kind, UUID uUID) {
        EasterBossEntity boss;
        ServerLevel level;
        Entity entity;
        void sparrow;
        void kind2;
        void owner;
        if (SparrowBorrowedSpells.validField(serverLevel, easterBossEntity, kind, uUID)) {
            return true;
        }
        return owner != null && ManorSparrowCasting.delegates((ManorSpellRules.Kind)kind2) && sparrow.getVariant() == 9 && ManorSparrowCasting.ready((EasterBossEntity)sparrow) && (entity = level.m_8791_((UUID)owner)) instanceof EasterBossEntity && ManorSparrowCasting.ready(boss = (EasterBossEntity)entity) && boss.getVariant() == kind2.variant && !boss.lemonSacrifice().active() && ManorSparrowCasting.sameFight(boss, (EasterBossEntity)sparrow) && boss.m_20280_((Entity)sparrow) <= 2304.0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick(ServerLevel level, EasterBossEntity sparrow) {
        EasterBossEntity boss;
        EasterBossEntity owner;
        LivingEntity living;
        Cast cast;
        Map<UUID, Cast> casts = ACTIVE.get(level);
        Cast cast2 = cast = casts == null ? null : casts.get(sparrow.m_20148_());
        if (cast == null) {
            return;
        }
        ManorSignatureSpell spell = ManorIronSpells.spell(cast.kind());
        long age = level.m_46467_() - cast.started();
        Entity entity = level.m_8791_(cast.target());
        LivingEntity target = entity instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
        Entity entity2 = level.m_8791_(cast.owner());
        EasterBossEntity easterBossEntity = owner = entity2 instanceof EasterBossEntity ? (boss = (EasterBossEntity)entity2) : null;
        if (!(ManorSparrowCasting.validField(level, sparrow, cast.kind(), cast.owner()) && owner != null && owner.m_5448_() == target && target != null && BossSpellEffects.canTarget((LivingEntity)sparrow, target) && BossSpellEffects.canTarget((LivingEntity)owner, target) && !(sparrow.m_20280_((Entity)target) > 1024.0) && sparrow.m_142582_((Entity)target) && spell.isEnabled() && age >= 0L && age <= (long)(cast.duration() + 20))) {
            ManorSparrowCasting.finish(level, sparrow, casts, cast, true);
            return;
        }
        sparrow.m_21563_().m_24960_((Entity)target, 60.0f, 60.0f);
        if (age < (long)cast.duration()) {
            cast.magic().handleCastDuration();
            spell.onServerCastTick((Level)level, cast.rank(), (LivingEntity)sparrow, cast.magic());
            return;
        }
        try {
            spell.onCast((Level)level, cast.rank(), (LivingEntity)sparrow, CastSource.MOB, cast.magic());
        }
        finally {
            ManorSparrowCasting.finish(level, sparrow, casts, cast, false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void finish(ServerLevel level, EasterBossEntity sparrow, Map<UUID, Cast> casts, Cast cast, boolean cancelled) {
        casts.remove(sparrow.m_20148_());
        try {
            ManorIronSpells.spell(cast.kind()).onServerCastComplete((Level)level, cast.rank(), (LivingEntity)sparrow, cast.magic(), cancelled);
        }
        finally {
            sparrow.finishParallelSpell();
            if (casts.isEmpty()) {
                ACTIVE.remove(level);
            }
        }
    }

    public static void unload(ServerLevel level) {
        Map<UUID, Cast> casts = ACTIVE.remove(level);
        if (casts == null) {
            return;
        }
        for (UUID identifier : List.copyOf(casts.keySet())) {
            Entity entity = level.m_8791_(identifier);
            if (!(entity instanceof EasterBossEntity)) continue;
            EasterBossEntity sparrow = (EasterBossEntity)entity;
            ManorSparrowCasting.finish(level, sparrow, casts, casts.get(identifier), true);
        }
    }

    public static void clear() {
        ACTIVE.clear();
    }

    private record Cast(UUID owner, UUID target, ManorSpellRules.Kind kind, long started, int duration, int rank, MagicData magic) {
    }
}

