package net.farmingtales.ftboss.mixin;

import java.util.UUID;
import net.farmingtales.ftboss.item.NativePaidCarrierState;
import net.farmingtales.ftboss.item.NativePaidWeaponCarrier;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativePaidWeapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    targets = {"net.minecraft.world.entity.projectile.Projectile"},
    remap = false
)
public abstract class NativePaidWeaponCarrierMixin implements NativePaidWeaponCarrier {
    @Shadow(
        remap = false
    )
    private UUID ownerUUID;
    @Unique
    private final NativePaidCarrierState<NativePaidWeapon> ftboss$paidWeapon = new NativePaidCarrierState<>();

    @Override
    public void ftboss$bindOwnerPaidWeapon(NativePaidWeapon var1) {
        this.ftboss$paidWeapon.bind(var1, this.ownerUUID);
    }

    @Override
    public NativePaidWeapon ftboss$originalOwnerPaidWeapon() {
        return this.ftboss$paidWeapon.original(this.ownerUUID);
    }

    @Override
    public void ftboss$retireOwnerPaidWeapon() {
        this.ftboss$paidWeapon.retire();
    }
}
