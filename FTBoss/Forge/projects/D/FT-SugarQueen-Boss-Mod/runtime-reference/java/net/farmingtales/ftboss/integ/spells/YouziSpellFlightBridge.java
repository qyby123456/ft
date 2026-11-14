/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import java.util.List;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.farmingtales.ftboss.youzi.spells.YouziSpellAssets;
import net.farmingtales.ftboss.youzi.spells.YouziStarProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class YouziSpellFlightBridge {
    public static boolean pulse(ServerLevel serverLevel, LivingEntity livingEntity, ManorSpellRules.Kind kind, Vec3 vec3, float f, int n) {
        if (kind != ManorSpellRules.Kind.EFFIGY_VOLLEY || YouziSpellAssets.geometry() == null) {
            return false;
        }
        if (!ManorSpellRules.pulse(n, 12, 3)) {
            return true;
        }
        double d = BossSpellTuning.radiusScale(livingEntity);
        Vec3 vec32 = livingEntity.m_20182_();
        List list = serverLevel.m_6249_(null, new AABB(vec32, vec32).m_82400_(64.0), entity -> entity instanceof YouziStarProjectile);
        if (n == 0) {
            for (Entity entity2 : list) {
                if (!((YouziStarProjectile)entity2).ownedBy(livingEntity)) continue;
                entity2.m_146870_();
            }
        }
        if (list.size() >= 64) {
            return true;
        }
        Vec3 vec33 = vec3.m_82546_(livingEntity.m_20182_());
        double d2 = Math.hypot(vec33.f_82479_, vec33.f_82481_);
        if (d2 < 1.0E-6) {
            vec33 = livingEntity.m_20154_();
        }
        double d3 = Math.atan2(-vec33.f_82479_, -vec33.f_82481_);
        YouziHomingFlight.Vector vector = YouziSpellAssets.geometry().launchOrigin(n / 12, d);
        double d4 = Math.cos(d3);
        double d5 = Math.sin(d3);
        Vec3 vec34 = vec32.m_82520_(vector.coordinateX() * d4 + vector.coordinateZ() * d5, vector.coordinateY(), -vector.coordinateX() * d5 + vector.coordinateZ() * d4);
        LivingEntity livingEntity2 = null;
        double d6 = Double.POSITIVE_INFINITY;
        for (LivingEntity livingEntity3 : ManorSpellEffects.targets(serverLevel, livingEntity, livingEntity.m_20182_(), 24.0)) {
            double d7;
            Vec3 vec35 = livingEntity3.m_20191_().m_82399_().m_82546_(vec34);
            Vec3 vec36 = vec3.m_82546_(vec34).m_82541_();
            if (vec35.m_82541_().m_82526_(vec36) < 0.25 || !((d7 = livingEntity3.m_20191_().m_82399_().m_82557_(vec3)) < d6)) continue;
            d6 = d7;
            livingEntity2 = livingEntity3;
        }
        Object object = new YouziStarProjectile(serverLevel, livingEntity, livingEntity2, vec34, vec3, f, d);
        if (serverLevel.m_7967_(object)) {
            SpellVisuals.show(serverLevel, livingEntity, kind.id, SpellVisualRules.Phase.PULSE, vec3);
        }
        return true;
    }

    public static boolean validTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        return BossSpellEffects.canTarget(livingEntity, livingEntity2);
    }

    public static void hit(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
        ManorSpellEffects.hurt(livingEntity, livingEntity2, ManorIronSpells.spell(ManorSpellRules.Kind.EFFIGY_VOLLEY), f);
    }
}

