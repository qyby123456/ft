/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.butcherskills;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.butcherskills.ButcherFxPlan;
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ButcherSkillFx {
    private static final DustParticleOptions CRIMSON = new DustParticleOptions(new Vector3f(0.85f, 0.06f, 0.12f), 0.75f);
    private static final DustParticleOptions BONE = new DustParticleOptions(new Vector3f(1.0f, 0.72f, 0.38f), 0.75f);
    private static final Map<ServerLevel, ButcherSkillRules.ParticleBudget> BUDGETS = new WeakHashMap<ServerLevel, ButcherSkillRules.ParticleBudget>();

    private ButcherSkillFx() {
    }

    private static void emit(ServerLevel serverLevel2, Vec3 vec3, Vec3 vec32, List<ButcherFxPlan.Point> list, boolean bl) {
        ButcherSkillRules.ParticleBudget particleBudget = BUDGETS.computeIfAbsent(serverLevel2, serverLevel -> new ButcherSkillRules.ParticleBudget());
        long l = serverLevel2.m_46467_();
        for (ButcherFxPlan.Point point : list) {
            if (!particleBudget.take(l, bl)) break;
            serverLevel2.m_8767_((ParticleOptions)(point.bone() ? BONE : CRIMSON), vec3.f_82479_ + vec32.f_82479_ * point.offsetX() - vec32.f_82481_ * point.offsetZ(), vec3.f_82480_ + point.height(), vec3.f_82481_ + vec32.f_82481_ * point.offsetX() + vec32.f_82479_ * point.offsetZ(), 1, 0.018, 0.018, 0.018, 0.0);
        }
    }

    public static void telegraph(ServerLevel serverLevel, ButcherSkillRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        if (n % 4 == 0) {
            ButcherSkillFx.emit(serverLevel, vec3, vec32, ButcherFxPlan.spell(skill, n), false);
        }
    }

    public static void release(ServerLevel serverLevel, ButcherSkillRules.Skill skill, Vec3 vec3, Vec3 vec32) {
        ButcherSkillFx.emit(serverLevel, vec3, vec32, ButcherFxPlan.spell(skill, skill.windup), false);
    }

    public static void summon(ServerLevel serverLevel, Vec3 vec3, int n, boolean bl) {
        ButcherSkillFx.emit(serverLevel, vec3, new Vec3(1.0, 0.0, 0.0), ButcherFxPlan.summon(bl, n), true);
    }
}

