package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ManorDefenseNetwork;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.farmingtales.ftboss.util.ManorDefenseRules.State;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 服务端防御：2026-10-13，每实体一份临时状态；客户端不参与减伤，退出战斗立即清空。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorDefense {
    private static final Map<EasterBossEntity, State> STATES = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Integer> ADAPTIVE = new WeakHashMap<>();

    private ManorDefense() {}

    private static boolean eligible(EasterBossEntity boss) {
        if (boss.level().isClientSide || boss.isMini() || !boss.isAlive() || boss.isRemoved()
                || boss.isManorIntroHidden() || boss.isInvisible() || boss.isXuanlanSkyHidden()
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(boss)
                || net.farmingtales.ftboss.boss.HiddenBossIntro.isActing(boss.getUUID())) return false;
        if (nativeProtection(boss)) return true;
        LivingEntity target = boss.getTarget();
        return !boss.isNoAi() && !boss.isInvulnerable() && target != null && target.isAlive()
                && !target.isRemoved() && target.level() == boss.level() && boss.distanceToSqr(target) <= 64.0 * 64.0;
    }

    private static boolean nativeProtection(EasterBossEntity boss) {
        return boss.getVariant() == 6 && boss.isXuanlanSkyCasting()
                || boss.getVariant() == 10 && boss.isManorMaidShielded()
                || boss.getVariant() == 11 && boss.isQizaiProtectedCasting();
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)) return;
        ManorDefenseRules.Profile profile = ManorDefenseRules.profile(boss.getVariant());
        if (profile == null) {
            clear(boss);
            return;
        }
        int previousKind = adaptiveKind(boss);
        int kind = ManorAdaptiveShield.activeKind(boss);
        if (kind == 0) ADAPTIVE.remove(boss);
        else ADAPTIVE.put(boss, kind);
        boolean heartbeat = level.getGameTime() % ManorDefenseRules.HEARTBEAT_TICKS == 0;
        if (!eligible(boss)) {
            State old = STATES.remove(boss);
            if (old != null || previousKind != kind || kind != 0 && heartbeat) ManorDefenseNetwork.sync(boss, null);
            return;
        }
        State state = STATES.get(boss);
        if (state == null || state.profile.variant() != boss.getVariant()) {
            state = new State(profile, level.getGameTime());
            STATES.put(boss, state);
        }
        int previous = state.phase;
        state.advance(level.getGameTime(), nativeProtection(boss));
        if (previous != state.phase || previousKind != kind
                || (state.phase != ManorDefenseRules.IDLE || kind != 0) && heartbeat) ManorDefenseNetwork.sync(boss, state);
    }

    public static void confirmedHit(EasterBossEntity boss, DamageSource source) {
        if (!eligible(boss) || source == null || source.is(DamageTypes.GENERIC_KILL)) return;
        State state = STATES.get(boss);
        if (state == null || nativeProtection(boss)) return;
        long now = boss.level().getGameTime();
        state.advance(now, false);
        if (state.confirmedHit(now)) {
            ManorDefenseNetwork.sync(boss, state);
            FTBossMod.LOGGER.debug("[FTBoss][防御] {} 预警 {} ticks, 防守 {} ticks, CD {} ticks",
                    state.profile.name(), ManorDefenseRules.WARNING_TICKS, state.profile.guardTicks(), state.profile.cooldownTicks());
        }
    }

    public static float reduce(EasterBossEntity boss, DamageSource source, float amount) {
        if (source == null || !eligible(boss) || nativeProtection(boss)) return amount;
        State state = STATES.get(boss);
        if (state == null) return amount;
        state.advance(boss.level().getGameTime(), false);
        Entity direct = source.getDirectEntity();
        Entity attacker = source.getEntity();
        Vec3 origin = attacker != null && attacker != boss ? attacker.position()
                : direct != null ? direct.position() : source.getSourcePosition();
        double frontDot = -1.0;
        if (state.profile.frontCosine() <= -1.0) frontDot = 1.0;
        else if (origin != null) {
            Vec3 toward = origin.subtract(boss.position());
            double length = Math.sqrt(toward.x * toward.x + toward.z * toward.z);
            double yaw = Math.toRadians(boss.yBodyRot);
            if (length > 1.0E-6) frontDot = (-Math.sin(yaw) * toward.x + Math.cos(yaw) * toward.z) / length;
        }
        boolean bypass = source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        return ManorDefenseRules.reduce(state.profile, state.phase, amount, source.is(DamageTypeTags.IS_PROJECTILE), frontDot, bypass);
    }

    public static float reduceAfterSharing(float amount, EasterBossEntity boss, DamageSource source) {
        return reduce(boss, source, amount);
    }

    public static State state(EasterBossEntity boss) { return STATES.get(boss); }

    public static int adaptiveKind(EasterBossEntity boss) { return ADAPTIVE.getOrDefault(boss, 0); }

    private static void clear(EasterBossEntity boss) {
        State old = STATES.remove(boss);
        Integer adaptive = ADAPTIVE.remove(boss);
        if (old != null || adaptive != null) ManorDefenseNetwork.sync(boss, null);
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss && !boss.level().isClientSide) clear(boss);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { STATES.clear(); ADAPTIVE.clear(); }
}
