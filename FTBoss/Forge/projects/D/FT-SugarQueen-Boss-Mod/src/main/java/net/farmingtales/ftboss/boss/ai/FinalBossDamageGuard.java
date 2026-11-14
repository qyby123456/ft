package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public final class FinalBossDamageGuard {
    public static final float MAX_FINAL_DAMAGE = Float.MAX_VALUE;

    private FinalBossDamageGuard() {}

    public static boolean applies(LivingEntity entity) {
        if (entity instanceof EasterBossEntity boss) return !boss.isMini();
        if (entity instanceof RatBossEntity) return false;
        return entity instanceof SugarQueenEntity || entity instanceof MeatMountainEntity
                || entity instanceof TeaTreeBossEntity || entity instanceof WineBarrelBossEntity
                || entity instanceof DefectCoreEntity || entity instanceof CompostBossEntity
                || entity instanceof AppleBossEntity || entity instanceof BengerEntity
                || entity instanceof TideFishBossEntity;
    }

    public static boolean blocked(float finalDamage) {
        return !Float.isFinite(finalDamage);
    }

    public static float settled(LivingEntity entity, DamageSource source, float finalDamage) {
        if (entity == null || !applies(entity) || entity.level().isClientSide()
                || source.is(DamageTypes.GENERIC_KILL)) return finalDamage;
        if (ManorDamageLimit.applies(entity)) return ManorDamageLimit.limit(entity, source, finalDamage);
        return blocked(finalDamage) ? 0.0F : finalDamage;
    }
}
