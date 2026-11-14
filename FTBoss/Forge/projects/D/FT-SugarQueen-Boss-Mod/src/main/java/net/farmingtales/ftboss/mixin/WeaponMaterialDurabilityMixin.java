package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 材料耐久覆盖 ItemStack 公共入口，损耗保留小数余量；2026-10-12 新增并兼容原版与模组物品调用。 */
@Mixin(value = ItemStack.class, remap = false)
public abstract class WeaponMaterialDurabilityMixin {
    @Inject(method = {"isDamageableItem()Z", "m_41763_()Z"}, at = @At("RETURN"), cancellable = true, remap = false)
    private void ftboss$staffDamageable(CallbackInfoReturnable<Boolean> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (callback.getReturnValue() || stack.isEmpty() || FoodWeaponFamilyBuild.kind(stack) != Kind.STAFF
                || stack.getItem().getMaxDamage(stack) > 0 || stack.hasTag() && stack.getTag().getBoolean("Unbreakable")) return;
        callback.setReturnValue(true);
    }

    @Inject(method = {"getMaxDamage()I", "m_41776_()I"}, at = @At("RETURN"), cancellable = true, remap = false)
    private void ftboss$materialMaxDamage(CallbackInfoReturnable<Integer> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        int original = callback.getReturnValue();
        int result = WeaponMaterialBuild.maxDurability(stack, original);
        if (result != original) callback.setReturnValue(result);
    }

    @ModifyVariable(method = {"hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z",
            "m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"},
            at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private int ftboss$materialWear(int requested) {
        return WeaponMaterialBuild.durabilityCost((ItemStack) (Object) this, requested);
    }

    @ModifyVariable(method = {"setDamageValue(I)V", "m_41721_(I)V"},
            at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private int ftboss$protectedDamage(int requested) {
        return FoodWeaponLifecycle.beforeDamageChange((ItemStack) (Object) this, requested);
    }

    @Inject(method = {"setDamageValue(I)V", "m_41721_(I)V"}, at = @At("RETURN"), remap = false)
    private void ftboss$wearLoss(int requested, org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        FoodWeaponLifecycle.afterDamageChange((ItemStack) (Object) this);
    }

    @Inject(method = {"hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z",
            "m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"},
            at = @At("RETURN"), cancellable = true, remap = false)
    private void ftboss$keepExhausted(int requested, RandomSource random, ServerPlayer player,
                                    CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(FoodWeaponLifecycle.finishHurt((ItemStack) (Object) this, callback.getReturnValue(), player));
    }
}
