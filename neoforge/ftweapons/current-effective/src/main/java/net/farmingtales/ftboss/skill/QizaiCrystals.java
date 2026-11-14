package net.farmingtales.ftboss.skill;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.qizaicross.TemporaryTriads;
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.skill.qizaiparty.MultiplayerCrystals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Pre;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class QizaiCrystals {
    private static final Map<ServerLevel, Map<UUID, QizaiCrystals.Formation>> ACTIVE = new WeakHashMap<>();

    private QizaiCrystals() {
    }

    public static boolean validOwner(EasterBossEntity boss) {
        return boss != null
            && boss.isAlive()
            && !boss.isRemoved()
            && (!boss.isNoAi() || boss.isQizaiUninterruptibleCasting())
            && boss.getVariant() == 11
            && !boss.isMini()
            && !boss.isGiant()
            && (!boss.isInvulnerable() || boss.isQizaiProtectedCasting())
            && !ManorFight.isFinaleMember(boss);
    }

    public static boolean target(EasterBossEntity var0, Entity var1) {
        if (!ManorLivingTargets.canHit(var0, var1)) {
            return false;
        } else {
            if (var1 instanceof LivingEntity var2
                && var2.isAlive()
                && !var2.isRemoved()
                && !ManorLivingTargets.creative(var2)
                && !var2.isSpectator()
                && var2.level() == var0.level()
                && var0.distanceToSqr(var2) <= 4096.0) {
                return true;
            }

            return false;
        }
    }

    private static QizaiCrystals.Formation formation(EasterBossEntity boss) {
        return boss != null && boss.level() instanceof ServerLevel level ? ACTIVE.getOrDefault(level, Map.of()).get(boss.getUUID()) : null;
    }

    public static List<QizaiCrystalEntity> members(EasterBossEntity boss) {
        QizaiCrystals.Formation formation = formation(boss);
        return CrossGuard.members(
            MultiplayerCrystals.members(
                TemporaryTriads.members(
                    formation == null
                        ? List.of()
                        : Arrays.stream(formation.crystals).filter(crystal -> crystal != null && crystal.isAlive() && !crystal.isRemoved()).toList(),
                    boss
                ),
                boss
            ),
            boss
        );
    }

    public static boolean current(QizaiCrystalEntity crystal) {
        QizaiCrystals.Formation formation = formation(crystal.getOwner());
        return CrossGuard.accept(
            MultiplayerCrystals.accept(TemporaryTriads.accept(formation != null && formation.crystals[crystal.role()] == crystal, crystal), crystal), crystal
        );
    }

    public static LivingEntity combatTarget(EasterBossEntity boss) {
        if (!validOwner(boss)) {
            return null;
        } else {
            QizaiCrystals.Formation formation = formation(boss);
            if (target(boss, boss.getTarget())) {
                if (formation != null) {
                    formation.target = boss.getTarget();
                }

                return boss.getTarget();
            } else if (formation != null && target(boss, formation.target)) {
                return formation.target;
            } else {
                return boss.level() instanceof ServerLevel level
                    ? level.players().stream().filter(player -> target(boss, player)).min(Comparator.comparingDouble(boss::distanceToSqr)).orElse(null)
                    : null;
            }
        }
    }

    public static void tick(EasterBossEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            MultiplayerCrystals.tick(boss);
        } else if (!validOwner(boss)) {
            cancel(boss);
            MultiplayerCrystals.tick(boss);
        } else {
            Map<UUID, QizaiCrystals.Formation> forms = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
            QizaiCrystals.Formation formation = forms.get(boss.getUUID());
            LivingEntity target = combatTarget(boss);
            if (target == null) {
                if (formation != null && ++formation.idle >= 100) {
                    cancel(boss);
                }

                MultiplayerCrystals.tick(boss);
            } else {
                if (formation == null) {
                    if (forms.size() >= 64) {
                        MultiplayerCrystals.tick(boss);
                        return;
                    }

                    formation = new QizaiCrystals.Formation();
                    forms.put(boss.getUUID(), formation);
                }

                formation.target = target;
                formation.idle = 0;
                long now = level.getGameTime();

                for (int role = 0; role < 3; role++) {
                    QizaiCrystalEntity crystal = formation.crystals[role];
                    if (crystal != null && (!crystal.isAlive() || crystal.isRemoved() || level.getEntity(crystal.getUUID()) != crystal)) {
                        crystal.discard();
                        formation.crystals[role] = null;
                        formation.ready[role] = now + 240L;
                    }

                    if (formation.crystals[role] == null && now >= formation.ready[role]) {
                        QizaiCrystalEntity summoned = new QizaiCrystalEntity(level, boss, role);
                        double angle = (double)role * Math.PI * 2.0 / 3.0;
                        Vec3 spawn = boss.position().add(Math.cos(angle) * 2.0, 2.8, Math.sin(angle) * 2.0);
                        summoned.setPos(spawn);
                        if (level.noCollision(summoned) && level.addFreshEntity(summoned)) {
                            formation.crystals[role] = summoned;
                        } else {
                            formation.ready[role] = now + 20L;
                        }
                    }
                }

                MultiplayerCrystals.tick(boss);
            }
        }
    }

    public static void cancel(EasterBossEntity boss) {
        if (boss.level() instanceof ServerLevel level) {
            Map<UUID, QizaiCrystals.Formation> forms = ACTIVE.get(level);
            QizaiCrystals.Formation formation = forms == null ? null : forms.remove(boss.getUUID());
            if (formation != null) {
                formation.close();
            }
        }

        TemporaryTriads.cancel(boss);
        MultiplayerCrystals.cancel(boss);
        CrossGuard.cancel(boss);
    }

    @SubscribeEvent
    public static void tick(Pre event) {
        if (event.getEntity() instanceof EasterBossEntity boss && (boss.getVariant() == 11 || formation(boss) != null)) {
            tick(boss);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void defend(LivingIncomingDamageEvent event) {
        if (!event.isCanceled()
            && event.getEntity() instanceof EasterBossEntity boss
            && combatTarget(boss) != null
            && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
            && Float.isFinite(event.getAmount())
            && !(event.getAmount() <= 0.0F)) {
            for (QizaiCrystalEntity crystal : members(boss)) {
                if (crystal.role() == 2 && crystal.distanceToSqr(boss) <= 256.0) {
                    event.setAmount(event.getAmount() - CrossGuard.absorb(crystal.absorb(event.getAmount() * 0.35F), event.getAmount(), crystal));
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss) {
            cancel(boss);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss) {
            cancel(boss);
        }
    }

    @SubscribeEvent
    public static void unload(Unload event) {
        MultiplayerCrystals.unload(event);
        Map<UUID, QizaiCrystals.Formation> forms = ACTIVE.remove(event.getLevel());
        if (forms != null) {
            forms.values().forEach(QizaiCrystals.Formation::close);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        MultiplayerCrystals.stop();
        ACTIVE.clear();
    }

    private static final class Formation {
        final QizaiCrystalEntity[] crystals = new QizaiCrystalEntity[3];
        final long[] ready = new long[3];
        LivingEntity target;
        int idle;

        void close() {
            for (QizaiCrystalEntity crystal : this.crystals) {
                if (crystal != null) {
                    crystal.discard();
                }
            }
        }
    }
}
