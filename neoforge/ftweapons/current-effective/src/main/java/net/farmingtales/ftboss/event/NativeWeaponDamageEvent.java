package net.farmingtales.ftboss.event;

import java.util.Objects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public abstract class NativeWeaponDamageEvent extends LivingEvent implements ICancellableEvent {
    private final DamageContainer container;

    private NativeWeaponDamageEvent(LivingEntity entity, DamageContainer container) {
        super(Objects.requireNonNull(entity, "entity"));
        this.container = Objects.requireNonNull(container, "container");
    }

    public final DamageSource getSource() {
        return this.container.getSource();
    }

    public final float getAmount() {
        return this.container.getNewDamage();
    }

    public final void setAmount(float amount) {
        this.container.setNewDamage(amount);
    }

    public static final class BeforeMitigation extends NativeWeaponDamageEvent {
        BeforeMitigation(LivingEntity entity, DamageContainer container) {
            super(entity, container);
        }
    }

    public static final class BeforeHealth extends NativeWeaponDamageEvent {
        BeforeHealth(LivingEntity entity, DamageContainer container) {
            super(entity, container);
        }
    }
}
