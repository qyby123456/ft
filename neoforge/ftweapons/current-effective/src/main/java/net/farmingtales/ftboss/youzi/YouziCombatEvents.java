package net.farmingtales.ftboss.youzi;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeHealth;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class YouziCombatEvents {
    public static final String[] IDENTIFIERS = new String[]{
        "clone_spawn", "clone_depart", "melee_swing", "hit_spark", "resonance_burst", "share_thread", "cast_focus", "ward_react", "echo_trace"
    };
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final Map<EasterBossEntity, YouziCombatEvents.State> STATES = new WeakHashMap<>();

    public static boolean validIdentifier(String var0) {
        return Arrays.asList(IDENTIFIERS).contains(var0);
    }

    private static boolean youzi(Entity var0) {
        if (var0 instanceof EasterBossEntity var1 && var1.getVariant() == 0) {
            return true;
        }

        return false;
    }

    private static YouziHomingFlight.Vector feet(Entity var0) {
        return new YouziHomingFlight.Vector(var0.getX(), var0.getY(), var0.getZ());
    }

    public static void show(Entity var0, String var1, YouziHomingFlight.Vector var2, YouziHomingFlight.Vector var3, double var4, double var6) {
        if (youzi(var0) && var0.level() instanceof ServerLevel var8 && validIdentifier(var1)) {
            send(var8, var1, var2, var3, var4, 180.0 - var6, var0);
        }
    }

    private static void send(ServerLevel var0, String var1, YouziHomingFlight.Vector var2, YouziHomingFlight.Vector var3, double var4, double var6, Entity var8) {
        if (Double.isFinite(var4) && !(var4 <= 0.0) && !(var4 > 4.0) && Double.isFinite(var6)) {
            YouziCombatPacket var9 = new YouziCombatPacket(
                var1,
                var0.dimension().location().toString(),
                SEQUENCE.incrementAndGet(),
                var0.getGameTime(),
                var2,
                var3,
                var4,
                var6,
                var8.getId(),
                var8.getUUID()
            );
            YouziCombatNetwork.send(var0, var9);
        }
    }

    public static void statueFeedback(ServerPlayer var0, int var1, Vec3 var2, Vec3 var3) {
        if (var0.level() instanceof ServerLevel var4 && var2 != null && var3 != null) {
            YouziHomingFlight.Vector var7 = new YouziHomingFlight.Vector(var2.x, var2.y, var2.z);
            YouziHomingFlight.Vector var6 = new YouziHomingFlight.Vector(var3.x, var3.y, var3.z);
            if (var1 == 2) {
                send(var4, "echo_trace", var7, var6, 1.0, 0.0, var0);
            }

            if (var1 == 4) {
                send(var4, "ward_react", var7, var6, Math.max(0.25, Math.min(4.0, (double)var0.getBbHeight() / 1.8)), (double)(180.0F - var0.getYRot()), var0);
            }

            return;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void joined(EntityJoinLevelEvent var0) {
        if (!var0.isCanceled() && !var0.loadedFromDisk() && var0.getLevel() instanceof ServerLevel && youzi(var0.getEntity())) {
            EasterBossEntity var1 = (EasterBossEntity)var0.getEntity();
            if (var1.isMini()) {
                show(var1, "clone_spawn", feet(var1), feet(var1), Math.max(0.25, Math.min(4.0, (double)var1.getBbHeight() / 0.9)), (double)var1.getYRot());
            }
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent var0) {
        if (var0.getLevel() instanceof ServerLevel && youzi(var0.getEntity())) {
            EasterBossEntity var1 = (EasterBossEntity)var0.getEntity();
            STATES.remove(var1);
            RemovalReason var2 = var1.getRemovalReason();
            if (var1.isMini() && (var2 == RemovalReason.KILLED || var2 == RemovalReason.DISCARDED)) {
                show(var1, "clone_depart", feet(var1), feet(var1), Math.max(0.25, Math.min(4.0, (double)var1.getBbHeight() / 0.9)), (double)var1.getYRot());
            }
        }
    }

    @SubscribeEvent
    public static void livingTick(EntityTickEvent.Pre var0) {
        if (var0.getEntity().level() instanceof ServerLevel && youzi(var0.getEntity())) {
            EasterBossEntity var1 = (EasterBossEntity)var0.getEntity();
            if (!var1.isAlive()) {
                STATES.remove(var1);
            } else {
                int var2 = var1.getActionTicks();
                boolean var3 = var1.isCastingLike();
                YouziCombatEvents.State var4 = STATES.put(var1, new YouziCombatEvents.State(var2, var3));
                if (var4 != null) {
                    double var5 = Math.max(0.25, Math.min(4.0, (double)var1.getBbHeight() / 1.8));
                    if (var2 > var4.action() && !var3) {
                        show(var1, "melee_swing", feet(var1), feet(var1), var5, (double)var1.getYRot());
                    }

                    if (var3 && !var4.casting()) {
                        show(var1, "cast_focus", feet(var1), feet(var1), var5, (double)var1.getYRot());
                    }
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void damage(BeforeHealth var0) {
        Entity var1 = var0.getSource().getEntity();
        if (!var0.isCanceled() && !(var0.getAmount() <= 0.0F) && youzi(var1) && var0.getEntity().level() instanceof ServerLevel) {
            LivingEntity var2 = var0.getEntity();
            YouziHomingFlight.Vector var3 = feet(var2).add(new YouziHomingFlight.Vector(0.0, (double)var2.getBbHeight() * 0.55, 0.0));
            show(var1, "hit_spark", var3, var3, 1.0, (double)var1.getYRot());
        }
    }

    public static void resonance(EasterBossEntity var0, LivingEntity var1) {
        YouziHomingFlight.Vector var2 = var1 == null ? feet(var0) : feet(var1);
        show(var0, "resonance_burst", var2, var2, Math.min(4.0, BossSpellTuning.radiusScale(var0)), (double)var0.getYRot());
    }

    public static void sharedDamage(EasterBossEntity var0, EasterBossEntity var1) {
        if (var1 != null && var1.isMini() && var1.getVariant() == 0) {
            show(var0, "share_thread", feet(var0), feet(var1), 1.0, 0.0);
        }
    }

    public static void guarded(Entity var0, LivingEntity var1) {
        if (var1 != null) {
            show(var0, "ward_react", feet(var1), feet(var1), 1.0, (double)var0.getYRot());
        }
    }

    public static void echo(Entity var0, YouziHomingFlight.Vector var1, YouziHomingFlight.Vector var2) {
        show(var0, "echo_trace", var1, var2, 1.0, 0.0);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent var0) {
        STATES.clear();
    }

    private static record State(int action, boolean casting) {
    }
}
