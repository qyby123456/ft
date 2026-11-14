package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 使用服务端实际位移锁定预判点，复用现有天罚与星阵预警。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorPredictiveAttacks {
    private static final Map<ServerLevel, List<ExtraSpell>> PENDING = new WeakHashMap<>();
    private static final Map<Player, Vec3> POSITIONS = new WeakHashMap<>();
    private static final Map<Player, Vec3> VELOCITIES = new WeakHashMap<>();

    private record ExtraSpell(EasterBossEntity boss, int kind, Vec3 mark, long releaseAt) {}

    private ManorPredictiveAttacks() {}

    private static boolean player(LivingEntity target, ServerLevel level) {
        return target instanceof Player player && player.isAlive() && !player.isRemoved()
                && !player.isCreative() && !player.isSpectator() && player.level() == level;
    }

    private static Vec3 prediction(LivingEntity target) {
        Vec3 velocity = VELOCITIES.get(target);
        double velocityX = velocity == null ? target.getX() - target.xo : velocity.x;
        double velocityZ = velocity == null ? target.getZ() - target.zo : velocity.z;
        double scale = ManorRefinementRules.leadScale(velocityX, velocityZ);
        return target.position().add(velocityX * scale, 0, velocityZ * scale);
    }

    private static Vec3 ground(ServerLevel level, EasterBossEntity boss, Vec3 point, Vec3 center, double range) {
        if (!Double.isFinite(point.x + point.y + point.z)
                || point.subtract(center).horizontalDistanceSqr() > range * range) return null;
        Vec3 arena = ManorFight.anchorOf(ManorFight.fightId(boss));
        if (arena != null && point.subtract(arena).horizontalDistanceSqr() > 76 * 76) return null;
        BlockPos column = BlockPos.containing(point);
        if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) return null;
        var hit = level.clip(new ClipContext(point.add(0, 1, 0), point.add(0, -12, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().add(0, 0.04, 0) : null;
    }

    public static Vec3 skyPrediction(ServerLevel level, EasterBossEntity boss, LivingEntity target, Vec3 home) {
        if (!player(target, level)) return null;
        return ground(level, boss, prediction(target), home, 64);
    }

    public static void extraSpell(ServerLevel level, EasterBossEntity boss, LivingEntity target, int spell) {
        if (!ManorRefinementRules.minorSpell(spell) || !player(target, level)
                || boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || !QizaiConstellation.active(boss)) return;
        Vec3 point = prediction(target);
        double angle = boss.getRandom().nextDouble() * Math.PI * 2;
        double jitter = boss.getRandom().nextDouble() * 1.5;
        Vec3 mark = ground(level, boss, point.add(Math.cos(angle) * jitter, 0, Math.sin(angle) * jitter),
                boss.position(), QizaiRules.RANGE);
        if (mark == null) mark = ground(level, boss, point, boss.position(), QizaiRules.RANGE);
        if (mark == null) return;
        int kind = boss.getRandom().nextBoolean() ? QizaiRules.STAR_CROSS : QizaiRules.ROSE_STARFALL;
        QizaiConstellation.warning(level, boss, kind, List.of(mark));
        PENDING.computeIfAbsent(level, ignored -> new ArrayList<>()).add(
                new ExtraSpell(boss, kind, mark, level.getGameTime() + ManorRefinementRules.PREDICTION_TICKS));
    }

    @SubscribeEvent
    public static void sample(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            Vec3 current = player.position();
            Vec3 previous = POSITIONS.put(player, current);
            VELOCITIES.put(player, previous == null ? Vec3.ZERO : current.subtract(previous));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<ExtraSpell> pending = PENDING.get(level);
        if (pending == null) return;
        pending.removeIf(extra -> {
            EasterBossEntity boss = extra.boss();
            if (boss.level() != level || !QizaiConstellation.active(boss)) return true;
            if (level.getGameTime() < extra.releaseAt()) return false;
            QizaiConstellation.cast(level, boss, extra.kind(), List.of(extra.mark()), victim -> {
                if (!player(victim, level) || boss.isAlliedTo(victim) || victim.isAlliedTo(boss)) return;
                int previous = victim.invulnerableTime;
                victim.invulnerableTime = 0;
                try {
                    float base = extra.kind() == QizaiRules.STAR_CROSS ? QizaiRules.CROSS_DAMAGE : QizaiRules.STARFALL_DAMAGE;
                    victim.hurt(level.damageSources().indirectMagic(boss, boss), boss.manorSkillDamage(victim, base, 2.0));
                } finally {
                    victim.invulnerableTime = Math.max(previous, victim.invulnerableTime);
                }
            });
            return true;
        });
        if (pending.isEmpty()) PENDING.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { PENDING.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        PENDING.clear();
        POSITIONS.clear();
        VELOCITIES.clear();
    }
}
