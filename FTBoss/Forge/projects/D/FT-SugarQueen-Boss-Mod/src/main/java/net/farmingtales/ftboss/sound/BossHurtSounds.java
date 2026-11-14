package net.farmingtales.ftboss.sound;

import net.farmingtales.ftboss.entity.RatBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public final class BossHurtSounds {
    private static final float VOLUME = 0.55F;
    private static final float PITCH_VARIATION = 0.12F;

    private BossHurtSounds() {}

    public static SoundEvent soundFor(BossHurtProfile profile) {
        if (profile == BossHurtProfile.BUTCHER) return SoundEvents.RAVAGER_HURT;
        if (profile == BossHurtProfile.BARREL || profile == BossHurtProfile.ROBOT) return SoundEvents.IRON_GOLEM_HURT;
        if (profile == BossHurtProfile.RAT) return SoundEvents.SILVERFISH_HURT;
        if (profile == BossHurtProfile.BENGER) return SoundEvents.WOLF_HURT;
        if (profile == BossHurtProfile.TIDE) return SoundEvents.GUARDIAN_HURT;
        return SoundEvents.PLAYER_HURT;
    }

    public static boolean play(LivingEntity entity) {
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (type == null) return false;
        BossHurtProfile profile = BossHurtProfile.fromType(type.getNamespace(), type.getPath());
        if (profile == null || entity instanceof RatBossEntity rat && rat.isMinion()) return false;
        float pitch = 1.0F + (entity.getRandom().nextFloat() - 0.5F) * PITCH_VARIATION;
        entity.playSound(soundFor(profile), VOLUME, pitch);
        return true;
    }
}
