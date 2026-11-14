/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={TridentItem.class})
public abstract class NativeTridentWearEntryMixin {
    @Redirect(method={"releaseUsing"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"), require=1, expect=1, allow=1)
    private void ftboss$v6$wear(ItemStack itemStack, int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer) {
        FoodWeaponSourceLedger.nativeWear(itemStack, n, livingEntity, consumer);
    }

    @Redirect(method={"releaseUsing"}, at=@At(value="NEW", target="(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/ThrownTrident;"), require=1, expect=1, allow=1)
    private ThrownTrident ftboss$v6$factory(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        return FoodWeaponSourceLedger.nativeTridentFactory(level, livingEntity, itemStack);
    }

    @Redirect(method={"releaseUsing"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), require=1, expect=1, allow=1)
    private boolean ftboss$v6$add(Level level, Entity entity) {
        return FoodWeaponSourceLedger.nativeAdd(level, entity);
    }
}

