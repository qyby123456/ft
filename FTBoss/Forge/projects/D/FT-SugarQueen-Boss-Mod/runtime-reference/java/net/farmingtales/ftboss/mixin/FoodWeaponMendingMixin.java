/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package net.farmingtales.ftboss.mixin;

import java.util.function.Predicate;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ExperienceOrb.class}, remap=false)
public abstract class FoodWeaponMendingMixin {
    @ModifyArg(method={"m_147092_(Lnet/minecraft/world/entity/player/Player;I)I"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;m_44839_(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;"), index=2, require=0, remap=false)
    @Group(name="ftbossMendingFilter", min=1, max=1)
    private Predicate<ItemStack> ftboss$srgMending(Predicate<ItemStack> predicate) {
        return predicate.and(itemStack -> !NativeFoodWeaponWear.eligible(itemStack));
    }
}

