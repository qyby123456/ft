package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeActionObservation.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class FoodWeaponItemUseMixin {
    @Redirect(
        method = {"use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "ftboss$itemUse",
        min = 1,
        max = 1
    )
    private InteractionResultHolder<ItemStack> ftboss$useSrg(Item var1, Level var2, Player var3, InteractionHand var4) {
        return NativeActionObservation.invoke(Action.USE, (ItemStack)(Object)this, var3, var4, () -> FoodWeaponUseBridge.use(var1, (ItemStack)(Object)this, var2, var3, var4));
    }

    @Redirect(
        method = {"releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "ftboss$itemRelease",
        min = 1,
        max = 1
    )
    private void ftboss$releaseSrg(Item var1, ItemStack var2, Level var3, LivingEntity var4, int var5) {
        NativeActionObservation.invoke(Action.RELEASE, var2, var4, var4.getUsedItemHand(), () -> {
            FoodWeaponUseBridge.release(var1, var2, var3, var4, var5);
            return null;
        });
    }

    @Redirect(
        method = {"onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "ftboss$itemTick",
        min = 1,
        max = 1
    )
    private void ftboss$tickSrg(Item var1, Level var2, LivingEntity var3, ItemStack var4, int var5) {
        NativeActionObservation.invoke(Action.USE_TICK, var4, var3, var3.getUsedItemHand(), () -> {
            FoodWeaponUseBridge.useTick(var1, var4, var2, var3, var5);
            return null;
        });
    }

    @Redirect(
        method = {"finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "ftboss$itemFinish",
        min = 1,
        max = 1
    )
    private ItemStack ftboss$finishSrg(Item var1, ItemStack var2, Level var3, LivingEntity var4) {
        return NativeActionObservation.invoke(Action.FINISH, var2, var4, var4.getUsedItemHand(), () -> FoodWeaponUseBridge.finish(var1, var2, var3, var4));
    }
}
