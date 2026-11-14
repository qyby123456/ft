package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.StackWalker.Option;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerTridentCarrierAccess;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AbstractArrow.class})
public abstract class OwnerTridentOriginalCopyMixin implements OwnerTridentCarrierAccess {
    @Unique
    private FoodWeaponSourceLedger.TridentCarrierLineage ftboss$ownerTridentLineage;

    @Unique
    @Override
    public final FoodWeaponSourceLedger.TridentCarrierLineage ftboss$ownerTridentLineage() {
        return this.ftboss$ownerTridentLineage;
    }

    @Unique
    @Override
    public final void ftboss$ownerInstallTridentLineage(FoodWeaponSourceLedger.TridentCarrierLineage var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0 -> var0.skip(1L).findFirst().map(var0x -> var0x.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false));
        if (var2 && this.ftboss$ownerTridentLineage == null) {
            this.ftboss$ownerTridentLineage = var1;
        }
    }

    @WrapOperation(
        method = {"<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
            ordinal = 0
        )},
        require = 1
    )
    private ItemStack ftboss$ownerTridentOriginalCopy(ItemStack var1, Operation<ItemStack> var2) {
        ItemStack var3 = (ItemStack)var2.call(new Object[]{var1});

        try {
            if ((Object)this instanceof ThrownTrident trident) FoodWeaponSourceLedger.ownerTridentOriginalCopy(trident, var1, var3);
        } catch (Throwable var5) {
        }

        return var3;
    }
    @WrapOperation(
        method = {"tryPickup(Lnet/minecraft/world/entity/player/Player;)Z"},
        at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")},
        require = 1
    )
    private boolean ftboss$ownerTridentPickup(net.minecraft.world.entity.player.Inventory inventory, ItemStack candidate,
        Operation<Boolean> original, net.minecraft.world.entity.player.Player player) {
        return FoodWeaponSourceLedger.nativeTridentPickupInventory((AbstractArrow)(Object)this, player, inventory, candidate, original);
    }

    @WrapOperation(
        method = {"playerTouch(Lnet/minecraft/world/entity/player/Player;)V"},
        at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V")},
        require = 1
    )
    private void ftboss$ownerTridentPickupTaken(net.minecraft.world.entity.player.Player player,
        net.minecraft.world.entity.Entity entity, int count, Operation<Void> original) {
        FoodWeaponSourceLedger.nativeTridentPickupTaken((AbstractArrow)(Object)this, player, entity, count, original);
    }

}

