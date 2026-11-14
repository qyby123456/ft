package net.farmingtales.ftboss.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

public final class NativeManorDamageEvents {
    private NativeManorDamageEvents() {
    }

    public static float onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        DamageContainer container = new DamageContainer(source, amount);
        NativeWeaponDamagePhases.beforeMitigation(entity, container);
        return container.getNewDamage();
    }

    public static NativeWeaponDamageEvent.BeforeHealth damageEvent(LivingEntity entity, DamageSource source, float amount) {
        return new NativeWeaponDamageEvent.BeforeHealth(entity, new DamageContainer(source, amount));
    }

    public static boolean post(NativeWeaponDamageEvent.BeforeHealth event) {
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
}
