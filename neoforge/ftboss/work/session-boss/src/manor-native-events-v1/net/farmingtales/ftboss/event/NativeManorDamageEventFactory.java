package net.farmingtales.ftboss.event;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
public final class NativeManorDamageEventFactory {
    private NativeManorDamageEventFactory() {}
    public static NativeWeaponDamageEvent.BeforeHealth create(LivingEntity boss,DamageContainer container) {
        return new NativeWeaponDamageEvent.BeforeHealth(boss,container);
    }
}
