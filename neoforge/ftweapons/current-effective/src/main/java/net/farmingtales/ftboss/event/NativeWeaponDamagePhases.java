package net.farmingtales.ftboss.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

public final class NativeWeaponDamagePhases {
    private NativeWeaponDamagePhases() {
    }

    public static boolean beforeMitigation(LivingEntity entity, DamageContainer container) {
        NativeWeaponDamageEvent.BeforeMitigation event = new NativeWeaponDamageEvent.BeforeMitigation(entity, container);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            container.setNewDamage(0.0F);
        }
        return event.isCanceled() || container.getNewDamage() <= 0.0F;
    }

    public static float beforeHealth(DamageContainer container, LivingEntity entity) {
        NativeWeaponDamageEvent.BeforeHealth event = new NativeWeaponDamageEvent.BeforeHealth(entity, container);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            container.setNewDamage(0.0F);
        }
        return container.getNewDamage();
    }
}
