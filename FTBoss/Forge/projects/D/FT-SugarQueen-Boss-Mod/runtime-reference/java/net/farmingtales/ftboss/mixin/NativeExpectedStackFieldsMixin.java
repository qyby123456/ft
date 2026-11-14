/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ItemStack.class}, remap=false)
public abstract class NativeExpectedStackFieldsMixin
implements NativeExpectedFieldViews.Stack {
    @Shadow(remap=false, aliases={"tag"})
    private CompoundTag f_41590_;
    @Shadow(remap=false)
    private CompoundTag capNBT;
    @Shadow(remap=false, aliases={"count"})
    private int f_41587_;
    @Shadow(remap=false, aliases={"item"})
    @Final
    private Item f_41589_;

    @Override
    public CompoundTag ftboss$rawStackTag() {
        return this.f_41590_;
    }

    @Override
    public CompoundTag ftboss$rawStackCaps() {
        return this.capNBT;
    }

    @Override
    public int ftboss$rawStackCount() {
        return this.f_41587_;
    }

    @Override
    public Item ftboss$rawStackItem() {
        return this.f_41589_;
    }
}

