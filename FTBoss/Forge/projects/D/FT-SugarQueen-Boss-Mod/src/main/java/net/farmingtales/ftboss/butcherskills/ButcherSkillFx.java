package net.farmingtales.ftboss.butcherskills;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.butcherskills.ButcherFxPlan.Point;
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules.ParticleBudget;
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules.Skill;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ButcherSkillFx {
    private static final DustParticleOptions CRIMSON = new DustParticleOptions(new Vector3f(0.85F, 0.06F, 0.12F), 0.75F);
    private static final DustParticleOptions BONE = new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.38F), 0.75F);
    private static final Map<ServerLevel, ParticleBudget> BUDGETS = new WeakHashMap<>();

    private ButcherSkillFx() {}

    private static void emit(ServerLevel level, Vec3 center, Vec3 forward, List<Point> points, boolean summon) {
        ParticleBudget budget = BUDGETS.computeIfAbsent(level, ignored -> new ParticleBudget());
        long now = level.getGameTime();
        for (Point point : points) {
            if (!budget.take(now, summon)) break;
            level.sendParticles(point.bone() ? BONE : CRIMSON,
                    center.x + forward.x * point.offsetX() - forward.z * point.offsetZ(),
                    center.y + point.height(),
                    center.z + forward.z * point.offsetX() + forward.x * point.offsetZ(), 1, 0.018, 0.018, 0.018, 0);
        }
    }

    public static void telegraph(ServerLevel level, Skill skill, Vec3 center, Vec3 forward, int elapsed) {
        if (elapsed % 4 == 0) emit(level, center, forward, ButcherFxPlan.spell(skill, elapsed), false);
    }

    public static void release(ServerLevel level, Skill skill, Vec3 center, Vec3 forward) {
        emit(level, center, forward, ButcherFxPlan.spell(skill, skill.windup), false);
    }

    public static void summon(ServerLevel level, Vec3 center, int elapsed, boolean boss) {
        emit(level, center, new Vec3(1, 0, 0), ButcherFxPlan.summon(boss, elapsed), true);
    }
}
