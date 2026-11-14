/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.sound;

import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.sound.BossHurtProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public final class BossHurtSounds {
    private static final float VOLUME = 0.55f;
    private static final float PITCH_VARIATION = 0.12f;

    private BossHurtSounds() {
    }

    public static SoundEvent soundFor(BossHurtProfile bossHurtProfile) {
        if (bossHurtProfile == BossHurtProfile.BUTCHER) {
            return SoundEvents.f_12360_;
        }
        if (bossHurtProfile == BossHurtProfile.BARREL || bossHurtProfile == BossHurtProfile.ROBOT) {
            return SoundEvents.f_12008_;
        }
        if (bossHurtProfile == BossHurtProfile.RAT) {
            return SoundEvents.f_12421_;
        }
        if (bossHurtProfile == BossHurtProfile.BENGER) {
            return SoundEvents.f_12621_;
        }
        if (bossHurtProfile == BossHurtProfile.TIDE) {
            return SoundEvents.f_12005_;
        }
        return SoundEvents.f_12323_;
    }

    public static boolean play(LivingEntity livingEntity) {
        RatBossEntity ratBossEntity;
        ResourceLocation resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey((Object)livingEntity.m_6095_());
        if (resourceLocation == null) {
            return false;
        }
        BossHurtProfile bossHurtProfile = BossHurtProfile.fromType(resourceLocation.m_135827_(), resourceLocation.m_135815_());
        if (bossHurtProfile == null || livingEntity instanceof RatBossEntity && (ratBossEntity = (RatBossEntity)livingEntity).isMinion()) {
            return false;
        }
        float f = 1.0f + (livingEntity.m_217043_().m_188501_() - 0.5f) * 0.12f;
        livingEntity.m_5496_(BossHurtSounds.soundFor(bossHurtProfile), 0.55f, f);
        return true;
    }
}

