/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"com.github.L_Ender.cataclysm.message.MessageSwingArm$Handler"}, remap=false)
public abstract class NativeCatLeftClickMixin {
    @Redirect(method={"lambda$handle$0"}, at=@At(value="INVOKE", target="Lcom/github/L_Ender/cataclysm/items/ILeftClick;onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"), require=0, remap=false)
    private static boolean ftboss$actualLeftClick(@Coerce Object object, ItemStack itemStack, LivingEntity livingEntity) {
        return NativeSpecialCaller.leftClick(object, itemStack, livingEntity);
    }
}

