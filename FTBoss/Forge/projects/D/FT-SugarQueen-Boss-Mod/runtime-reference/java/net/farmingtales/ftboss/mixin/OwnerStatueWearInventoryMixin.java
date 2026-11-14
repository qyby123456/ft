/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.world.entity.player.Inventory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Inventory.class})
public abstract class OwnerStatueWearInventoryMixin {
    @Inject(method={"load(Lnet/minecraft/nbt/ListTag;)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$loadAnchor(ListTag listTag, CallbackInfo callbackInfo) {
    }
}

