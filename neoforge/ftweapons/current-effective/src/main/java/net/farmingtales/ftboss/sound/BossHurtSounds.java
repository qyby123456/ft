package net.farmingtales.ftboss.sound;

import net.farmingtales.ftboss.entity.RatBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BossHurtSounds {
    private static final float VOLUME = 0.55F;
    private static final float PITCH_VARIATION = 0.12F;

    private BossHurtSounds() {
    }

    public static SoundEvent soundFor(BossHurtProfile var0) {
        if (var0 == BossHurtProfile.BUTCHER) {
            return SoundEvents.RAVAGER_HURT;
        } else if (var0 == BossHurtProfile.BARREL || var0 == BossHurtProfile.ROBOT) {
            return SoundEvents.IRON_GOLEM_HURT;
        } else if (var0 == BossHurtProfile.RAT) {
            return SoundEvents.SILVERFISH_HURT;
        } else if (var0 == BossHurtProfile.BENGER) {
            return SoundEvents.WOLF_HURT;
        } else {
            return var0 == BossHurtProfile.TIDE ? SoundEvents.GUARDIAN_HURT : SoundEvents.PLAYER_HURT;
        }
    }

    public static boolean play(LivingEntity var0) {
        ResourceLocation var1 = BuiltInRegistries.ENTITY_TYPE.getKey(var0.getType());
        if (var1 == null) {
            return false;
        } else {
            BossHurtProfile var2 = BossHurtProfile.fromType(var1.getNamespace(), var1.getPath());
            if (var2 == null) {
                return false;
            } else {
                if (var0 instanceof RatBossEntity var3 && var3.isMinion()) {
                    return false;
                }

                float var4 = 1.0F + (var0.getRandom().nextFloat() - 0.5F) * 0.12F;
                var0.playSound(soundFor(var2), 0.55F, var4);
                return true;
            }
        }
    }
}
