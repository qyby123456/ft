package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.MaqueFlightRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class MaqueFeatherCombat {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<>();
    private static final String READY = "ftboss:maque_feather_ready";

    private MaqueFeatherCombat() {}

    private static boolean ready(EasterBossEntity sparrow) {
        return sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT && sparrow.isAlive()
                && !sparrow.isRemoved() && !sparrow.isInvulnerable() && ManorSpellCombat.eligible(sparrow);
    }

    private static boolean valid(ServerLevel level, EasterBossEntity sparrow, EasterBossEntity owner, LivingEntity target) {
        return ready(sparrow) && owner != null && owner.isAlive() && !owner.isRemoved() && !owner.isMini()
                && !owner.isInvulnerable() && ManorSpellCombat.eligible(owner)
                && owner.getVariant() != EasterBossEntity.SPARROW_VARIANT && owner.level() == level
                && Objects.equals(ManorFightMembers.fightId(sparrow), ManorFightMembers.fightId(owner))
                && Objects.equals(sparrow.getManorSummonerId(), owner.getManorSummonerId())
                && sparrow.distanceToSqr(owner) <= MaqueFlightRules.OWNER_RANGE * MaqueFlightRules.OWNER_RANGE
                && target != null && target.level() == level && owner.getTarget() == target
                && BossSpellEffects.canTarget(sparrow, target) && BossSpellEffects.canTarget(owner, target)
                && sparrow.distanceToSqr(target) <= MaqueFlightRules.TARGET_RANGE * MaqueFlightRules.TARGET_RANGE
                && sparrow.hasLineOfSight(target);
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity sparrow)
                || sparrow.getVariant() != EasterBossEntity.SPARROW_VARIANT
                || !(sparrow.level() instanceof ServerLevel level)) return;
        Map<UUID, Cast> casts = ACTIVE.get(level);
        long now = level.getGameTime();
        if (casts != null && now % 20 == 0) casts.entrySet().removeIf(entry -> level.getEntity(entry.getKey()) == null);
        Cast cast = casts == null ? null : casts.get(sparrow.getUUID());
        ManorSignatureSpell spell = ManorIronSpells.spell(Kind.FEATHER_FAN);
        if (cast != null) {
            EasterBossEntity owner = level.getEntity(cast.owner()) instanceof EasterBossEntity boss ? boss : null;
            LivingEntity target = level.getEntity(cast.target()) instanceof LivingEntity living ? living : null;
            int state = MaqueFlightRules.castState(now - cast.started(), cast.duration(),
                    valid(level, sparrow, owner, target), spell.isEnabled());
            if (state < 0) {
                finish(sparrow, casts, cast, true);
                sparrow.getPersistentData().putLong(READY, now + MaqueFlightRules.CANCEL_RETRY);
                return;
            }
            sparrow.getLookControl().setLookAt(target, 60, 60);
            cast.magic().setAdditionalCastData(new BossSignatureSpell.Aim(target.position().add(0, 0.5, 0)));
            if (state == 0) {
                cast.magic().handleCastDuration();
                spell.onServerCastTick(level, cast.rank(), sparrow, cast.magic());
                return;
            }
            try {
                spell.onCast(level, cast.rank(), sparrow, CastSource.MOB, cast.magic());
            } finally {
                finish(sparrow, casts, cast, false);
            }
            return;
        }
        if (!ready(sparrow) || !spell.isEnabled()) return;
        if (!sparrow.getPersistentData().contains(READY, 99)) {
            sparrow.getPersistentData().putLong(READY, now + MaqueFlightRules.INITIAL_DELAY + Math.floorMod(sparrow.getId(), 20));
            return;
        }
        if (!MaqueFlightRules.canStart(now, sparrow.getPersistentData().getLong(READY), casts == null ? 0 : casts.size(),
                sparrow.isCastingLike() || sparrow.isPerformingSkill() || ManorSparrowCasting.isCastingFor(level, sparrow))) return;
        EasterBossEntity owner = level.getEntitiesOfClass(EasterBossEntity.class,
                sparrow.getBoundingBox().inflate(MaqueFlightRules.OWNER_RANGE),
                candidate -> valid(level, sparrow, candidate, candidate.getTarget()))
                .stream().min(Comparator.comparingDouble(sparrow::distanceToSqr)).orElse(null);
        if (owner == null) {
            sparrow.getPersistentData().putLong(READY, now + 10);
            return;
        }
        LivingEntity target = owner.getTarget();
        int rank = Math.max(spell.getMinLevel(), Math.min(2, spell.getMaxLevel()));
        int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, sparrow)));
        MagicData magic = new MagicData(true);
        magic.setSyncedData(new SyncedSpellData(sparrow));
        if (!spell.checkPreCastConditions(level, rank, sparrow, magic)) {
            sparrow.getPersistentData().putLong(READY, now + MaqueFlightRules.CANCEL_RETRY);
            return;
        }
        magic.initiateCast(spell, rank, duration, CastSource.MOB, "mainhand");
        spell.onServerPreCast(level, rank, sparrow, magic);
        Vec3 point = target.position().add(0, 0.5, 0);
        magic.setAdditionalCastData(new BossSignatureSpell.Aim(point));
        ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>()).put(sparrow.getUUID(),
                new Cast(owner.getUUID(), target.getUUID(), now, duration, rank, magic));
        sparrow.getPersistentData().putLong(READY, now + MaqueFlightRules.COOLDOWN_TICKS);
        sparrow.getPersistentData().putLong("ftboss:manor_cd_feather_fan", now + MaqueFlightRules.COOLDOWN_TICKS);
        sparrow.getLookControl().setLookAt(target, 60, 60);
        sparrow.playParallelSpell(duration);
        ManorSpellEffects.warning(level, sparrow, Kind.FEATHER_FAN, point);
    }

    private static void finish(EasterBossEntity sparrow, Map<UUID, Cast> casts, Cast cast, boolean cancelled) {
        casts.remove(sparrow.getUUID());
        try {
            ManorIronSpells.spell(Kind.FEATHER_FAN).onServerCastComplete(sparrow.level(), cast.rank(), sparrow, cast.magic(), cancelled);
        } finally {
            sparrow.finishParallelSpell();
        }
    }

    public static boolean afterHit(boolean hit, LivingEntity caster, LivingEntity target, ManorSignatureSpell spell) {
        if (!hit || spell.kind != Kind.FEATHER_FAN || !(target.level() instanceof ServerLevel) || !target.isAlive()) return hit;
        MobEffect effect = FTEffects.HEAL_BAN.get();
        MobEffectInstance current = target.getEffect(effect);
        if (current == null || MaqueFlightRules.refreshBan(current.getDuration(), current.isInfiniteDuration())) {
            int amplifier = current == null ? 0 : Math.max(0, current.getAmplifier());
            target.addEffect(new MobEffectInstance(effect, MaqueFlightRules.HEAL_BAN_TICKS, amplifier, false, true, true), caster);
        }
        return hit;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Map<UUID, Cast> casts = ACTIVE.remove(level);
        if (casts == null) return;
        for (UUID identifier : List.copyOf(casts.keySet())) {
            if (level.getEntity(identifier) instanceof EasterBossEntity sparrow) finish(sparrow, casts, casts.get(identifier), true);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); }

    private record Cast(UUID owner, UUID target, long started, int duration, int rank, MagicData magic) {}
}
