package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
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
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell.Aim;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.farmingtales.ftboss.xueli_multiplayer.CadenceServer;
import net.farmingtales.ftboss.zheergenvoidbody.VoidSignals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Pre;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class ManorSpellCombat {
    private static final Map<ServerLevel, Map<UUID, ManorSpellCombat.Cast>> ACTIVE = new WeakHashMap<>();
    private static final String READY = "ftboss:manor_spell_ready";
    private static final String NEXT = "ftboss:manor_spell_next";
    private static final int MAX_ACTIVE = 64;

    private ManorSpellCombat() {
    }

    private static int nextInterval(int variant) {
        return (int)Math.round((double)ManorBossCombatRules.spellInterval(variant) * ManorBossCombatRules.spellIntervalMultiplier(variant));
    }

    public static boolean eligible(EasterBossEntity boss) {
        return boss.getVariant() != 11
            && ManorSpellRules.eligible(boss.isMini() && !YouziCloneSpellProfile.isClone(boss), boss.isGiant(), boss.isNoAi(), ManorFight.isFinaleMember(boss));
    }

    private static boolean valid(EasterBossEntity boss, LivingEntity target) {
        return eligible(boss)
            && boss.isAlive()
            && !boss.isInvulnerable()
            && target != null
            && target.level() == boss.level()
            && BossSpellEffects.canTarget(boss, target)
            && boss.distanceToSqr(target) <= (double)(YouziCloneSpellProfile.isClone(boss) ? 256 : 1024)
            && boss.hasLineOfSight(target);
    }

    public static boolean replace(EasterBossEntity boss, LivingEntity target, String id) {
        if (boss.level() instanceof ServerLevel level && eligible(boss)) {
            Kind kind = ManorSpellRules.pair(boss.getVariant()).stream().filter(candidate -> candidate.id.equals(id)).findFirst().orElse(null);
            if (kind != null && ManorIronSpells.spell(kind).isEnabled()) {
                if (!valid(boss, target)) {
                    return true;
                }

                if (boss.lemonSacrifice().active()) {
                    Map<UUID, ManorSpellCombat.Cast> previous = ACTIVE.get(level);
                    if (previous != null) {
                        finish(level, boss, previous, true);
                    }

                    LemonSpellBarrage.tick(level, boss, target);
                    return true;
                }

                Map<UUID, ManorSpellCombat.Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
                long now = level.getGameTime();
                if (!casts.containsKey(boss.getUUID())
                    && !ManorSparrowCasting.isCastingFor(level, boss)
                    && (!YouziCloneSpellProfile.isClone(boss) || now >= boss.getPersistentData().getLong("ftboss:manor_spell_ready"))
                    && now >= boss.getPersistentData().getLong("ftboss:manor_cd_" + kind.id)) {
                    start(level, boss, target, kind, casts, now);
                    return true;
                }

                return true;
            }

            return false;
        }

        return false;
    }

    private static boolean start(ServerLevel level, EasterBossEntity boss, LivingEntity target, Kind kind, Map<UUID, ManorSpellCombat.Cast> casts, long now) {
        if (casts.size() >= 64) {
            return false;
        } else {
            List<Kind> enabled = ManorSpellRules.pair(boss.getVariant())
                .stream()
                .filter(candidate -> ManorIronSpells.spell(candidate).isEnabled())
                .filter(candidate -> boss.getVariant() != 9 || candidate.support())
                .toList();
            boolean clone = YouziCloneSpellProfile.isClone(boss);
            int spellCount = clone ? 1 : ManorBossCombatRules.spellsPerCast(boss.getVariant());
            Kind secondary = spellCount < 2
                ? null
                : enabled.stream()
                    .filter(candidate -> candidate != kind && now >= boss.getPersistentData().getLong("ftboss:manor_cd_" + candidate.id))
                    .findFirst()
                    .orElse(null);
            if (spellCount > 1 && secondary == null && enabled.size() >= spellCount) {
                return false;
            } else {
                int delegated = spellCount == 1 ? ManorSparrowCasting.start(level, boss, target, kind) : 0;
                if (delegated > 0) {
                    boss.getPersistentData()
                        .putLong(
                            "ftboss:manor_cd_" + kind.id,
                            CadenceServer.cooldown(now + (long)Math.max(100, ManorIronSpells.spell(kind).getSpellCooldown()), boss, now)
                        );
                    boss.getPersistentData()
                        .putLong(
                            "ftboss:manor_spell_ready",
                            CadenceServer.ready(now + (long)delegated + (long)ManorBossCombatRules.spellInterval(boss.getVariant()), boss, now, delegated)
                        );
                    return true;
                } else {
                    ManorSignatureSpell spell = ManorIronSpells.spell(kind);
                    int rank = Math.max(spell.getMinLevel(), Math.min(2, spell.getMaxLevel()));
                    int duration = Math.max(1, Math.min(200, spell.getEffectiveCastTime(rank, boss)));
                    MagicData magic = new MagicData(true);
                    magic.setSyncedData(new SyncedSpellData(boss));
                    if (!spell.checkPreCastConditions(level, rank, boss, magic)) {
                        return false;
                    } else {
                        MagicData secondaryMagic = null;
                        if (secondary != null) {
                            ManorSignatureSpell secondSpell = ManorIronSpells.spell(secondary);
                            secondaryMagic = new MagicData(true);
                            secondaryMagic.setSyncedData(new SyncedSpellData(boss));
                            if (!secondSpell.checkPreCastConditions(level, rank, boss, secondaryMagic)) {
                                return false;
                            }

                            duration = Math.max(duration, Math.max(1, Math.min(200, secondSpell.getEffectiveCastTime(rank, boss))));
                            secondaryMagic.initiateCast(secondSpell, rank, duration, CastSource.MOB, "offhand");
                            secondSpell.onServerPreCast(level, rank, boss, secondaryMagic);
                            secondaryMagic.setAdditionalCastData(new Aim(secondary.support() ? boss.position() : BossSpellEffects.aim(boss)));
                            ManorSpellEffects.warning(level, boss, secondary, BossSpellEffects.aim(boss));
                        }

                        magic.initiateCast(spell, rank, duration, CastSource.MOB, "mainhand");
                        spell.onServerPreCast(level, rank, boss, magic);
                        Vec3 center = kind.support() ? boss.position() : BossSpellEffects.aim(boss);
                        magic.setAdditionalCastData(new Aim(center));
                        casts.put(
                            boss.getUUID(), new ManorSpellCombat.Cast(kind, target.getUUID(), center, now, duration, rank, magic, secondary, secondaryMagic)
                        );
                        CompoundTag data = boss.getPersistentData();
                        data.remove("ftboss:manor_spell_until");
                        data.putLong(
                            "ftboss:manor_cd_" + kind.id,
                            CadenceServer.cooldown(now + (long)YouziCloneSpellProfile.cooldown(boss, spell.getSpellCooldown()), boss, now)
                        );
                        if (secondary != null) {
                            data.putLong(
                                "ftboss:manor_cd_" + secondary.id,
                                CadenceServer.cooldown(now + (long)Math.max(100, ManorIronSpells.spell(secondary).getSpellCooldown()), boss, now)
                            );
                        }

                        data.putLong(
                            "ftboss:manor_spell_ready",
                            CadenceServer.ready(now + (long)duration + (long)(clone ? 200 : nextInterval(boss.getVariant())), boss, now, duration)
                        );
                        VoidSignals.begin(boss, duration, kind, secondary);
                        CastServer.iron(boss, kind, secondary, duration);
                        BodySignals.iron(boss, kind, duration);
                        ZicaiCastSync.start(boss, kind, duration);
                        ManorSpellEffects.warning(level, boss, kind, center);
                        QingyiCastSync.start(boss, kind, duration);
                        return true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void tick(Pre event) {
        if (event.getEntity() instanceof EasterBossEntity boss && boss.level() instanceof ServerLevel level) {
            ManorSparrowCasting.tick(level, boss);
            Map<UUID, ManorSpellCombat.Cast> var16 = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
            long now = level.getGameTime();
            if (now % 20L == 0L) {
                var16.entrySet().removeIf(entry -> level.getEntity((UUID)entry.getKey()) == null);
            }

            LivingEntity target = boss.getTarget();
            ManorSpellCombat.Cast cast = (ManorSpellCombat.Cast)var16.get(boss.getUUID());
            if (!valid(boss, target)) {
                finish(level, boss, var16, true);
                LemonSpellBarrage.cancel(boss);
                return;
            }

            if (boss.lemonSacrifice().active()) {
                finish(level, boss, var16, true);
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
                if (target.getUUID().equals(cast.target())
                    && cast.kind().variant == boss.getVariant()
                    && spell.isEnabled()
                    && (cast.secondary() == null || ManorIronSpells.spell(cast.secondary()).isEnabled())
                    && age >= 0L
                    && age <= (long)(cast.duration() + 20)
                    && !spell.shouldAIStopCasting(cast.rank(), boss, target)) {
                    if (age < (long)cast.duration()) {
                        cast.magic().handleCastDuration();
                        spell.onServerCastTick(level, cast.rank(), boss, cast.magic());
                        if (cast.secondary() != null) {
                            cast.secondaryMagic().handleCastDuration();
                            ManorIronSpells.spell(cast.secondary()).onServerCastTick(level, cast.rank(), boss, cast.secondaryMagic());
                        }

                        return;
                    }

                    try {
                        VoidSignals.cast(spell, level, cast.rank(), boss, CastSource.MOB, cast.magic());
                        QingyiCastSync.release(boss);
                        ZicaiCastSync.release(boss);
                        if (cast.secondary() != null && boss.isAlive()) {
                            VoidSignals.cast(ManorIronSpells.spell(cast.secondary()), level, cast.rank(), boss, CastSource.MOB, cast.secondaryMagic());
                        }
                    } finally {
                        finish(level, boss, var16, false);
                    }

                    return;
                }

                finish(level, boss, var16, true);
                return;
            }

            boss.getPersistentData().remove("ftboss:manor_spell_until");
            CompoundTag data = boss.getPersistentData();
            if (!data.contains("ftboss:manor_spell_ready", 99)) {
                boolean clone = YouziCloneSpellProfile.isClone(boss);
                data.putLong("ftboss:manor_spell_ready", now + (long)(clone ? 40 : 100) + (long)Math.floorMod(boss.getId(), 40));
                if (clone) {
                    data.putInt("ftboss:manor_spell_next", Math.floorMod(boss.getId(), ManorSpellRules.pair(boss.getVariant()).size()));
                }

                return;
            }

            if (now >= data.getLong("ftboss:manor_spell_ready") && !(boss.distanceToSqr(target) > 576.0)) {
                List<Kind> pair = ManorSpellRules.pair(boss.getVariant());

                for (int offset = 0; offset < pair.size(); offset++) {
                    int index = Math.floorMod(data.getInt("ftboss:manor_spell_next") + offset, pair.size());
                    Kind kind = pair.get(index);
                    if ((boss.getVariant() != 9 || kind.support())
                        && ManorIronSpells.spell(kind).isEnabled()
                        && now >= data.getLong("ftboss:manor_cd_" + kind.id)) {
                        if (start(level, boss, target, kind, var16, now)) {
                            data.putInt("ftboss:manor_spell_next", (index + 1) % pair.size());
                        }
                        break;
                    }
                }

                return;
            }

            return;
        }
    }

    private static void finish(ServerLevel level, EasterBossEntity boss, Map<UUID, ManorSpellCombat.Cast> casts, boolean cancelled) {
        ManorSpellCombat.Cast cast = casts.remove(boss.getUUID());
        boss.getPersistentData().remove("ftboss:manor_spell_until");
        if (cast != null) {
            ManorIronSpells.spell(cast.kind()).onServerCastComplete(level, cast.rank(), boss, cast.magic(), cancelled);
            if (cast.secondary() != null) {
                ManorIronSpells.spell(cast.secondary()).onServerCastComplete(level, cast.rank(), boss, cast.secondaryMagic(), cancelled);
            }

            VoidSignals.complete(boss, cancelled);
            QingyiCastSync.finish(boss, cancelled);
            CastServer.finishIron(boss, cancelled);
            ZicaiCastSync.finish(boss, cancelled);
        }
    }

    @SubscribeEvent
    public static void unload(Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ManorSparrowCasting.unload(level);
            LemonSpellBarrage.unload(level);
            Map<UUID, ManorSpellCombat.Cast> casts = ACTIVE.remove(level);
            if (casts != null) {
                for (UUID id : List.copyOf(casts.keySet())) {
                    if (level.getEntity(id) instanceof EasterBossEntity boss) {
                        finish(level, boss, casts, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ManorSparrowCasting.clear();
        ACTIVE.clear();
        LemonSpellBarrage.clear();
    }

    private static record Cast(
        Kind kind, UUID target, Vec3 center, long started, int duration, int rank, MagicData magic, Kind secondary, MagicData secondaryMagic
    ) {
    }
}
