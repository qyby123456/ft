package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class LemonSpellBarrage {
    private static final Map<ServerLevel, Map<UUID, Burst>> ACTIVE = new WeakHashMap<>();
    private static final String READY = "ftboss:manor_spell_ready";
    private static final int MAX_ACTIVE = 64;

    private LemonSpellBarrage() {}

    private static List<AbstractSpell> pool(EasterBossEntity boss) {
        Map<String, AbstractSpell> spells = new LinkedHashMap<>();
        ManorSpellRules.pair(boss.getVariant()).forEach(kind -> {
            AbstractSpell spell = ManorIronSpells.spell(kind);
            spells.put(spell.getSpellId(), spell);
        });
        boss.lemonSacrifice().spells().forEach(kind -> {
            AbstractSpell spell = BossIronSpells.spell(kind);
            spells.put(spell.getSpellId(), spell);
        });
        boolean queen = boss.lemonSacrifice().has("sugar_queen");
        boolean tea = boss.lemonSacrifice().has("tea_tree");
        if (queen || tea || boss.lemonSacrifice().has("wine_barrel")) {
            WizardSummon.spellPool().stream().filter(spell -> queen || tea || WizardSummon.isFire(spell))
                    .forEach(spell -> spells.putIfAbsent(spell.getSpellId(), spell));
        }
        if (tea) WizardSummon.spellPoolOf(new String[]{"OAKSKIN_SPELL", "ABSORPTION_SPELL"})
                .forEach(spell -> spells.putIfAbsent(spell.getSpellId(), spell));
        return spells.values().stream().filter(AbstractSpell::isEnabled).toList();
    }

    private static String cooldownKey(AbstractSpell spell) {
        if (spell instanceof ManorSignatureSpell) return "ftboss:manor_cd_" + spell.getSpellResource().getPath();
        if (spell instanceof BossSignatureSpell) return BossSpellCombat.READY + ":" + spell.getSpellResource().getPath();
        return "ftboss:lemon_iron_cd:" + spell.getSpellId();
    }

    private static int rank(EasterBossEntity boss, AbstractSpell spell) {
        int desired = spell instanceof ManorSignatureSpell || spell instanceof BossSignatureSpell ? 1
                : (boss.lemonSacrifice().has("tea_tree") ? 6 : boss.lemonSacrifice().has("wine_barrel") ? 4 : 3)
                        + BossRage.tierOf(boss);
        return BossIronSpellLevels.castLevel(boss, spell,
                Math.max(spell.getMinLevel(), Math.min(desired, spell.getMaxLevel())));
    }

    public static void tick(ServerLevel level, EasterBossEntity boss, LivingEntity target) {
        Map<UUID, Burst> casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        long now = level.getGameTime();
        if (now % 20 == 0) casts.entrySet().removeIf(entry -> level.getEntity(entry.getKey()) == null);
        Burst burst = casts.get(boss.getUUID());
        if (burst != null) {
            long age = now - burst.started();
            if (!boss.lemonSacrifice().active() || !target.getUUID().equals(burst.target())
                    || burst.inheritedUntil() != boss.lemonSacrifice().state().getLong("until")
                    || burst.spells().size() > boss.lemonSacrifice().spellsPerCast()
                    || age < 0 || age > burst.duration() + 20
                    || burst.spells().stream().anyMatch(cast -> !cast.spell().isEnabled()
                            || cast.spell().shouldAIStopCasting(cast.rank(), boss, target))) {
                cancel(boss);
                return;
            }
            if (burst.lastTick() == now) return;
            casts.put(boss.getUUID(), new Burst(burst.target(), burst.inheritedUntil(), burst.started(),
                    burst.duration(), now, burst.spells()));
            for (SpellCast cast : burst.spells()) {
                try {
                    aimed(boss, target, () -> {
                        if (age < burst.duration()) {
                            cast.magic().handleCastDuration();
                            cast.spell().onServerCastTick(level, cast.rank(), boss, cast.magic());
                            if (cast.spell().getCastType() == CastType.CONTINUOUS && age % 10 == 0) {
                                cast.spell().onCast(level, cast.rank(), boss, CastSource.MOB, cast.magic());
                            }
                        } else if (cast.spell().getCastType() != CastType.CONTINUOUS && boss.isAlive()) {
                            cast.spell().onCast(level, cast.rank(), boss, CastSource.MOB, cast.magic());
                        }
                    });
                } catch (RuntimeException failure) {
                    FTBossMod.LOGGER.warn("[FTBoss][Lemon] barrage spell failed {}: {}", cast.spell().getSpellId(), failure.toString());
                    cancel(boss);
                    return;
                }
            }
            if (age >= burst.duration()) finish(level, boss, false);
            return;
        }
        if (!boss.getPersistentData().contains(READY, 99)) {
            boss.getPersistentData().putLong(READY, now + 100 + Math.floorMod(boss.getId(), 40));
            return;
        }
        if (now < boss.getPersistentData().getLong(READY) || casts.size() >= MAX_ACTIVE
                || boss.distanceToSqr(target) > ManorSpellRules.RANGE * ManorSpellRules.RANGE) return;
        List<AbstractSpell> available = pool(boss);
        if (available.isEmpty()) return;
        List<SpellCast> selected = new ArrayList<>();
        int next = Math.floorMod(boss.lemonSacrifice().state().getInt("barrage_next"), available.size());
        int duration = 1;
        for (int offset = 0; offset < available.size() && selected.size() < boss.lemonSacrifice().spellsPerCast(); offset++) {
            int index = (next + offset) % available.size();
            AbstractSpell spell = available.get(index);
            if (now < boss.getPersistentData().getLong(cooldownKey(spell))) continue;
            if (spell.getSpellId().startsWith("irons_spellbooks:") && !BossSpellPacing.tryLegacyCast(boss, spell)) continue;
            int rank = rank(boss, spell);
            MagicData magic = new MagicData(true);
            magic.setSyncedData(new SyncedSpellData(boss));
            try {
                boolean[] allowed = {false};
                aimed(boss, target, () -> allowed[0] = spell.checkPreCastConditions(level, rank, boss, magic));
                if (!allowed[0]) continue;
                int castTicks = spell.getEffectiveCastTime(rank, boss);
                if (!(spell instanceof ManorSignatureSpell) && spell.getCastType() == CastType.LONG) {
                    castTicks = boss.getChallengeDifficulty().acceleratedTicks(castTicks);
                }
                duration = Math.max(duration, Math.max(1, Math.min(200, castTicks)));
                selected.add(new SpellCast(spell, rank, magic));
                boss.lemonSacrifice().state().putInt("barrage_next", (index + 1) % available.size());
            } catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] skipped incompatible spell {}: {}", spell.getSpellId(), failure.toString());
            }
        }
        if (selected.isEmpty()) return;
        Burst started = new Burst(target.getUUID(), boss.lemonSacrifice().state().getLong("until"), now,
                duration, now, List.copyOf(selected));
        casts.put(boss.getUUID(), started);
        for (SpellCast cast : selected) {
            try {
                cast.magic().initiateCast(cast.spell(), cast.rank(), duration, CastSource.MOB, "mainhand");
                aimed(boss, target, () -> cast.spell().onServerPreCast(level, cast.rank(), boss, cast.magic()));
                int cooldown = cast.spell() instanceof BossSignatureSpell
                        ? BossSpellPacing.signatureCooldown(cast.spell().getSpellCooldown())
                        : Math.max(100, cast.spell().getSpellCooldown());
                boss.getPersistentData().putLong(cooldownKey(cast.spell()), now + cooldown);
            } catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] barrage preparation failed: {}", failure.toString());
                cancel(boss);
                return;
            }
        }
        boss.getPersistentData().remove(ManorSpellRules.LOCK_KEY);
        boss.getPersistentData().remove(BossSpellCombat.CAST);
        boss.getPersistentData().putLong(READY, now + duration + ManorBossCombatRules.spellInterval(boss.getVariant()));
        boss.playParallelSpell(duration);
    }

    private static void aimed(EasterBossEntity boss, LivingEntity target, Runnable action) {
        float yaw = boss.getYRot();
        float pitch = boss.getXRot();
        Vec3 direction = target.getEyePosition().subtract(boss.getEyePosition());
        boss.setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        boss.setXRot((float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
        try {
            boss.lemonSacrifice().runInherited(action);
        } finally {
            boss.setYRot(yaw);
            boss.setXRot(pitch);
        }
    }

    public static void cancel(EasterBossEntity boss) {
        if (boss.level() instanceof ServerLevel level) finish(level, boss, true);
    }

    private static void finish(ServerLevel level, EasterBossEntity boss, boolean cancelled) {
        Map<UUID, Burst> casts = ACTIVE.get(level);
        Burst burst = casts == null ? null : casts.remove(boss.getUUID());
        if (burst == null) return;
        for (SpellCast cast : burst.spells()) {
            try {
                boss.lemonSacrifice().runInherited(() -> cast.spell()
                        .onServerCastComplete(level, cast.rank(), boss, cast.magic(), cancelled));
            } catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] barrage completion failed: {}", failure.toString());
            } finally {
                cast.magic().resetCastingState();
            }
        }
        boss.finishParallelSpell();
    }

    static void unload(ServerLevel level) {
        Map<UUID, Burst> casts = ACTIVE.get(level);
        if (casts == null) return;
        for (UUID id : List.copyOf(casts.keySet())) {
            if (level.getEntity(id) instanceof EasterBossEntity boss) cancel(boss);
        }
        ACTIVE.remove(level);
    }

    static void clear() { ACTIVE.clear(); }

    private record SpellCast(AbstractSpell spell, int rank, MagicData magic) {}
    private record Burst(UUID target, long inheritedUntil, long started, int duration, long lastTick, List<SpellCast> spells) {}
}
