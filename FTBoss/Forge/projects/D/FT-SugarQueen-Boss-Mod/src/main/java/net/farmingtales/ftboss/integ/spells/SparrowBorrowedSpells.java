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
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorHealthBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.util.ManorSupportRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class SparrowBorrowedSpells {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<>();
    private static final String NEXT = "ftboss_sparrow_borrow_next";
    private static final int MAX_ACTIVE = 64;

    private SparrowBorrowedSpells() {}

    private static boolean ready(EasterBossEntity boss) {
        return boss != null && boss.isAlive() && !boss.isRemoved() && !boss.isInvulnerable()
                && !boss.isMini() && !boss.isGiant() && !boss.isNoAi();
    }

    private static boolean sameFight(EasterBossEntity boss, EasterBossEntity bird) {
        return Objects.equals(ManorFightMembers.fightId(boss), ManorFightMembers.fightId(bird))
                && Objects.equals(boss.getManorSummonerId(), bird.getManorSummonerId());
    }

    public static boolean validField(ServerLevel level, EasterBossEntity bird, Kind kind, UUID owner) {
        return owner != null && kind.variant != EasterBossEntity.SPARROW_VARIANT && ready(bird)
                && ManorHealthBalance.support(bird) && level.getEntity(owner) instanceof EasterBossEntity boss
                && EasterBossEntity.isManorVariant(boss.getVariant()) && ready(boss) && sameFight(boss, bird)
                && !boss.lemonSacrifice().active() && boss.distanceToSqr(bird) <= 48 * 48;
    }

    public static UUID ownerOf(LivingEntity caster, Kind kind) {
        if (!(caster.level() instanceof ServerLevel level)) return null;
        Map<UUID, Cast> casts = ACTIVE.get(level);
        Cast cast = casts == null ? null : casts.get(caster.getUUID());
        return cast != null && cast.kind() == kind ? cast.owner() : null;
    }

    public static boolean busy(LivingEntity caster) {
        return caster.level() instanceof ServerLevel level && ACTIVE.containsKey(level)
                && ACTIVE.get(level).containsKey(caster.getUUID());
    }

    private static boolean target(ServerLevel level, EasterBossEntity bird, EasterBossEntity boss, LivingEntity target) {
        return target instanceof ServerPlayer && target.level() == level && BossSpellEffects.canTarget(bird, target)
                && BossSpellEffects.canTarget(boss, target) && bird.distanceToSqr(target) <= ManorSpellRules.RANGE * ManorSpellRules.RANGE
                && bird.hasLineOfSight(target);
    }

    public static void tick(EasterBossEntity bird, EasterBossEntity boss) {
        if (!(bird.level() instanceof ServerLevel level) || !ready(bird) || !ready(boss)
                || !ManorHealthBalance.support(bird) || !sameFight(boss, bird) || bird.distanceToSqr(boss) > 48 * 48
                || !EasterBossEntity.isManorVariant(boss.getVariant()) || boss.lemonSacrifice().active()) return;
        long now = level.getGameTime();
        var data = bird.getPersistentData();
        long next = data.getLong(NEXT);
        if (!data.contains(NEXT) || next - now > ManorSupportRules.INTERVAL) {
            data.putLong(NEXT, now + ManorSupportRules.INTERVAL);
            return;
        }
        if (now < next || bird.isCastingLike() || bird.isPerformingSkill()) return;
        Map<UUID, Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        casts.entrySet().removeIf(entry -> level.getEntity(entry.getKey()) == null);
        if (casts.containsKey(bird.getUUID()) || casts.size() >= MAX_ACTIVE) return;
        LivingEntity victim = target(level, bird, boss, boss.getTarget()) ? boss.getTarget()
                : level.players().stream().filter(player -> target(level, bird, boss, player))
                .min(Comparator.comparingDouble(bird::distanceToSqr)).orElse(null);
        if (victim == null) return;
        List<Kind> pool = ManorSupportRules.pool(bird.getRandom().nextInt(ManorSupportRules.variants())).stream()
                .filter(kind -> ManorIronSpells.spell(kind).isEnabled()).toList();
        data.putLong(NEXT, now + ManorSupportRules.INTERVAL);
        if (pool.isEmpty()) return;
        Kind kind = pool.get(bird.getRandom().nextInt(pool.size()));
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        int rank = spell.getMinLevel();
        int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, bird)));
        MagicData magic = new MagicData(true);
        magic.setSyncedData(new SyncedSpellData(bird));
        if (!spell.checkPreCastConditions(level, rank, bird, magic)) return;
        Vec3 point = kind.support() ? boss.position() : victim.position().add(0, 0.5, 0);
        bird.getLookControl().setLookAt(victim, 60, 60);
        magic.initiateCast(spell, rank, duration, CastSource.MOB, "mainhand");
        spell.onServerPreCast(level, rank, bird, magic);
        magic.setAdditionalCastData(new BossSignatureSpell.Aim(point));
        casts.put(bird.getUUID(), new Cast(boss.getUUID(), victim.getUUID(), kind, now, duration, rank, magic));
        bird.playParallelSpell(duration);
        ManorSpellEffects.warning(level, bird, kind, point);
    }

    private static void advance(ServerLevel level, EasterBossEntity bird, Map<UUID, Cast> casts, Cast cast) {
        long age = level.getGameTime() - cast.started();
        LivingEntity victim = level.getEntity(cast.target()) instanceof LivingEntity living ? living : null;
        EasterBossEntity boss = level.getEntity(cast.owner()) instanceof EasterBossEntity owner ? owner : null;
        ManorSignatureSpell spell = ManorIronSpells.spell(cast.kind());
        if (!validField(level, bird, cast.kind(), cast.owner()) || boss == null || !target(level, bird, boss, victim)
                || !spell.isEnabled() || age < 0 || age > cast.duration() + 20) {
            finish(level, bird, casts, cast, true);
            return;
        }
        bird.getLookControl().setLookAt(victim, 60, 60);
        if (age < cast.duration()) {
            cast.magic().handleCastDuration();
            spell.onServerCastTick(level, cast.rank(), bird, cast.magic());
            return;
        }
        try {
            spell.onCast(level, cast.rank(), bird, CastSource.MOB, cast.magic());
        } finally {
            finish(level, bird, casts, cast, false);
        }
    }

    private static void finish(ServerLevel level, EasterBossEntity bird, Map<UUID, Cast> casts, Cast cast, boolean cancelled) {
        casts.remove(bird.getUUID());
        try {
            ManorIronSpells.spell(cast.kind()).onServerCastComplete(level, cast.rank(), bird, cast.magic(), cancelled);
        } finally {
            bird.finishParallelSpell();
        }
    }

    public static boolean allied(LivingEntity caster, Entity target) {
        return caster.isAlliedTo(target) || target instanceof LivingEntity living
                && (friendly(caster, living) || friendly(living, caster));
    }

    public static boolean friendly(Entity caster, LivingEntity target) {
        if (target == null) return false;
        Entity current = caster;
        for (int depth = 0; current != null && depth < 8; depth++) {
            if (current instanceof EasterBossEntity bird && bird.getVariant() == EasterBossEntity.SPARROW_VARIANT) {
                var type = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
                UUID fight = ManorFightMembers.fightId(bird);
                return target == bird || bird.isAlliedTo(target) || target.isAlliedTo(bird)
                        || type != null && FTBossMod.MOD_ID.equals(type.getNamespace())
                        || !(target instanceof Player) && fight != null && fight.equals(ManorFightMembers.fightId(target))
                        || ManorFightMembers.ownerId(target) != null && (ManorFightMembers.ownerId(target).equals(bird.getUUID())
                        || ManorFightMembers.ownerId(target).equals(ManorFightMembers.ownerId(bird)));
            }
            Entity parent = ManorFightMembers.owner(current);
            if (parent == current) break;
            current = parent;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (friendly(event.getSource().getEntity(), event.getEntity())
                || friendly(event.getSource().getDirectEntity(), event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        Map<UUID, Cast> casts = ACTIVE.get(level);
        if (casts == null) return;
        for (UUID identity : List.copyOf(casts.keySet())) {
            if (level.getEntity(identity) instanceof EasterBossEntity bird) advance(level, bird, casts, casts.get(identity));
            else casts.remove(identity);
        }
        if (casts.isEmpty()) ACTIVE.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { ACTIVE.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); }

    private record Cast(UUID owner, UUID target, Kind kind, long started, int duration, int rank, MagicData magic) {}
}
