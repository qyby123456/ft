/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.bengeranimation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.farmingtales.ftboss.bengeranimation.BengerEffectPlan;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

public final class BengerSkillEffects {
    private static final Map<BengerEffectPlan.Point, DustParticleOptions> PARTICLES = BengerSkillEffects.buildParticles();

    private static Map<BengerEffectPlan.Point, DustParticleOptions> buildParticles() {
        HashMap<BengerEffectPlan.Point, DustParticleOptions> hashMap = new HashMap<BengerEffectPlan.Point, DustParticleOptions>();
        for (String string : BengerEffectPlan.ROLES) {
            for (int i = 0; i <= BengerEffectPlan.duration(string); ++i) {
                for (BengerEffectPlan.Point point2 : BengerEffectPlan.frame(string, i)) {
                    hashMap.computeIfAbsent(point2, point -> new DustParticleOptions(new Vector3f(point.red(), point.green(), point.blue()), point.size()));
                }
            }
        }
        return Map.copyOf(hashMap);
    }

    public static void tick(LivingEntity livingEntity, String string, int n, int n2, int n3, int n4, int n5, int n6, double d, double d2, double d3) {
        ServerLevel serverLevel;
        Object object;
        block6: {
            block5: {
                object = livingEntity.m_9236_();
                if (!(object instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)object;
                if (!livingEntity.m_21224_()) break block6;
            }
            return;
        }
        object = BengerEffectPlan.emission(string, n, n2, n3, n4, n5, n6);
        if (object.isEmpty()) {
            return;
        }
        double d4 = Math.toRadians(livingEntity.m_146908_());
        double d5 = Math.cos(d4);
        double d6 = Math.sin(d4);
        double d7 = livingEntity.m_20185_();
        double d8 = livingEntity.m_20186_();
        double d9 = livingEntity.m_20189_();
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            BengerEffectPlan.Point point = (BengerEffectPlan.Point)iterator.next();
            boolean bl = point.anchor().equals("target");
            double d10 = bl ? d + point.offsetX() : d7 + point.offsetX() * d5 + point.offsetZ() * d6;
            double d11 = (bl ? d2 : d8) + point.offsetY();
            double d12 = bl ? d3 + point.offsetZ() : d9 + point.offsetX() * d6 - point.offsetZ() * d5;
            serverLevel.m_8767_((ParticleOptions)PARTICLES.get(point), d10, d11, d12, 1, 0.015, 0.01, 0.015, 0.0);
        }
    }
}

