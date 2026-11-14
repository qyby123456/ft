/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.youzi.facing;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.youzi.facing.FacingMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class YouziFacing {
    private YouziFacing() {
    }

    private static boolean supports(String string) {
        return "effigy_volley".equals(string) || "effigy_ward".equals(string) || "effigy_procession".equals(string);
    }

    private static boolean owner(LivingEntity livingEntity) {
        EasterBossEntity easterBossEntity;
        return livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 0 && !easterBossEntity.m_21525_() && easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && !easterBossEntity.m_20147_();
    }

    public static boolean face(LivingEntity livingEntity, String string) {
        if (!(YouziFacing.supports(string) && YouziFacing.owner(livingEntity) && livingEntity.m_9236_() instanceof ServerLevel)) {
            return false;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
        LivingEntity livingEntity2 = easterBossEntity.m_5448_();
        if (livingEntity2 == null || livingEntity2 == livingEntity || livingEntity2.m_9236_() != livingEntity.m_9236_() || !livingEntity2.m_6084_() || livingEntity2.m_213877_() || livingEntity2.m_20147_() || !BossSpellEffects.canTarget(livingEntity, livingEntity2) || !livingEntity.m_142582_((Entity)livingEntity2)) {
            return false;
        }
        Vec3 vec3 = livingEntity2.m_146892_().m_82546_(livingEntity.m_146892_());
        if (!Double.isFinite(vec3.f_82479_) || !Double.isFinite(vec3.f_82480_) || !Double.isFinite(vec3.f_82481_) || vec3.m_82556_() < 1.0E-12) {
            return false;
        }
        float f = FacingMath.yaw(vec3.f_82479_, vec3.f_82481_, livingEntity.m_146908_());
        float f2 = FacingMath.pitch(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, livingEntity.m_146909_());
        if (!Float.isFinite(f) || !Float.isFinite(f2)) {
            return false;
        }
        easterBossEntity.m_21563_().m_24960_((Entity)livingEntity2, 180.0f, 180.0f);
        livingEntity.m_146922_(f);
        livingEntity.m_146926_(f2);
        livingEntity.m_5616_(f);
        livingEntity.m_5618_(f);
        return true;
    }

    public static Vec3 direction(Vec3 vec3, LivingEntity livingEntity, String string) {
        if (!"effigy_ward".equals(string) || !YouziFacing.owner(livingEntity)) {
            return vec3;
        }
        Vec3 vec32 = livingEntity.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
        return Double.isFinite(vec32.f_82479_) && Double.isFinite(vec32.f_82481_) && vec32.m_82556_() > 1.0E-12 ? vec32 : vec3;
    }

    public static double modelYaw(double d, LivingEntity livingEntity, SpellVisualPacket spellVisualPacket, float f) {
        if (!YouziFacing.owner(livingEntity) || spellVisualPacket == null || !YouziFacing.supports(spellVisualPacket.spell().id)) {
            return d;
        }
        boolean bl = spellVisualPacket.spell() == SpellVisualRules.Spell.EFFIGY_WARD || spellVisualPacket.spell() == SpellVisualRules.Spell.EFFIGY_VOLLEY && spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
        return bl ? FacingMath.modelYaw(livingEntity.f_20886_, livingEntity.f_20885_, f, d) : d;
    }
}

