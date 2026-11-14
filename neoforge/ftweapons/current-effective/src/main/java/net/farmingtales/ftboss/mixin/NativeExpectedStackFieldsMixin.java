package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Stack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class NativeExpectedStackFieldsMixin implements Stack {
    @Shadow(
        remap = false,
        aliases = {"tag"}
    )
    private CompoundTag tag;
    @Shadow(
        remap = false
    )
    private CompoundTag capNBT;
    @Shadow(
        remap = false,
        aliases = {"count"}
    )
    private int count;
    @Shadow(
        remap = false,
        aliases = {"item"}
    )
    @Final
    private Item item;

    @Override
    public CompoundTag ftboss$rawStackTag() {
        return this.tag;
    }

    @Override
    public CompoundTag ftboss$rawStackCaps() {
        return this.capNBT;
    }

    @Override
    public int ftboss$rawStackCount() {
        return this.count;
    }

    @Override
    public Item ftboss$rawStackItem() {
        return this.item;
    }
}
