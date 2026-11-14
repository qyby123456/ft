package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ManorDefenseNetwork;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorDefense {
    private static final Map<EasterBossEntity, ManorDefenseRules.State> STATES = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Integer> ADAPTIVE = new WeakHashMap<>();

    private ManorDefense() {
    }

    private static boolean eligible(EasterBossEntity boss) {
        if (boss.level().isClientSide
            || boss.isMini()
            || !boss.isAlive()
            || boss.isRemoved()
            || boss.isManorIntroHidden()
            || boss.isInvisible()
            || boss.isXuanlanSkyHidden()
            || ManorFight.isFinaleMember(boss)
            || HiddenBossIntro.isActing(boss.getUUID())) {
            return false;
        } else if (nativeProtection(boss)) {
            return true;
        } else {
            LivingEntity target = boss.getTarget();
            return !boss.isNoAi()
                && !boss.isInvulnerable()
                && target != null
                && target.isAlive()
                && !target.isRemoved()
                && target.level() == boss.level()
                && boss.distanceToSqr(target) <= 4096.0;
        }
    }

    private static boolean nativeProtection(EasterBossEntity boss) {
        return boss.getVariant() == 6 && boss.isXuanlanSkyCasting()
            || boss.getVariant() == 10 && ((NativeManorDefenseAccess)boss).isManorMaidShielded()
            || boss.getVariant() == 11 && boss.isQizaiProtectedCasting();
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof EasterBossEntity boss && boss.level() instanceof ServerLevel level) {
            ManorDefenseRules.Profile profile = ManorDefenseRules.profile(boss.getVariant());
            if (profile == null) {
                clear(boss);
                return;
            }

            int previousKind = adaptiveKind(boss);
            int kind = ManorAdaptiveShield.activeKind(boss);
            if (kind == 0) {
                ADAPTIVE.remove(boss);
            } else {
                ADAPTIVE.put(boss, kind);
            }

            boolean heartbeat = level.getGameTime() % 20L == 0L;
            if (!eligible(boss)) {
                ManorDefenseRules.State old = STATES.remove(boss);
                if (old != null || previousKind != kind || kind != 0 && heartbeat) {
                    ManorDefenseNetwork.sync(boss, null);
                }

                return;
            }

            ManorDefenseRules.State state = STATES.get(boss);
            if (state == null || state.profile.variant() != boss.getVariant()) {
                state = new ManorDefenseRules.State(profile, level.getGameTime());
                STATES.put(boss, state);
            }

            int previous = state.phase;
            state.advance(level.getGameTime(), nativeProtection(boss));
            if (previous != state.phase || previousKind != kind || (state.phase != 0 || kind != 0) && heartbeat) {
                ManorDefenseNetwork.sync(boss, state);
            }

            return;
        }
    }

    public static void confirmedHit(EasterBossEntity boss, DamageSource source) {
        if (eligible(boss) && source != null && !source.is(DamageTypes.GENERIC_KILL)) {
            ManorDefenseRules.State state = STATES.get(boss);
            if (state != null && !nativeProtection(boss)) {
                long now = boss.level().getGameTime();
                state.advance(now, false);
                if (state.confirmedHit(now)) {
                    ManorDefenseNetwork.sync(boss, state);
                    FTBossMod.LOGGER
                        .debug(
                            "[FTBoss][防御] {} 预警 {} ticks, 防守 {} ticks, CD {} ticks",
                            state.profile.name(),
                            12,
                            state.profile.guardTicks(),
                            state.profile.cooldownTicks()
                        );
                }
            }
        }
    }

    public static float reduce(EasterBossEntity boss, DamageSource source, float amount) {
        if (source != null && eligible(boss) && !nativeProtection(boss)) {
            ManorDefenseRules.State state = STATES.get(boss);
            if (state == null) {
                return amount;
            } else {
                state.advance(boss.level().getGameTime(), false);
                Entity direct = source.getDirectEntity();
                Entity attacker = source.getEntity();
                Vec3 origin = attacker != null && attacker != boss ? attacker.position() : (direct != null ? direct.position() : source.getSourcePosition());
                double frontDot = -1.0;
                if (state.profile.frontCosine() <= -1.0) {
                    frontDot = 1.0;
                } else if (origin != null) {
                    Vec3 toward = origin.subtract(boss.position());
                    double length = Math.sqrt(toward.x * toward.x + toward.z * toward.z);
                    double yaw = Math.toRadians((double)boss.yBodyRot);
                    if (length > 1.0E-6) {
                        frontDot = (-Math.sin(yaw) * toward.x + Math.cos(yaw) * toward.z) / length;
                    }
                }

                boolean bypass = source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
                return ManorDefenseRules.reduce(state.profile, state.phase, amount, source.is(DamageTypeTags.IS_PROJECTILE), frontDot, bypass);
            }
        } else {
            return amount;
        }
    }

    public static float reduceAfterSharing(float amount, EasterBossEntity boss, DamageSource source) {
        return reduce(boss, source, amount);
    }

    public static ManorDefenseRules.State state(EasterBossEntity boss) {
        return STATES.get(boss);
    }

    public static int adaptiveKind(EasterBossEntity boss) {
        return ADAPTIVE.getOrDefault(boss, 0);
    }

    private static void clear(EasterBossEntity boss) {
        ManorDefenseRules.State old = STATES.remove(boss);
        Integer adaptive = ADAPTIVE.remove(boss);
        if (old != null || adaptive != null) {
            ManorDefenseNetwork.sync(boss, null);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss && !boss.level().isClientSide) {
            clear(boss);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        STATES.clear();
        ADAPTIVE.clear();
    }
}
