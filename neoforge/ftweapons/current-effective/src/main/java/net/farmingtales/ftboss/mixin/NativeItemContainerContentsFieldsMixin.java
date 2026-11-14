package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.farmingtales.ftboss.item.NativeNestedItemFields;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemContainerContents.class, remap = false)
public abstract class NativeItemContainerContentsFieldsMixin implements NativeNestedItemFields.Items {
    @Shadow(remap = false)
    @Final
    private NonNullList<ItemStack> items;

    @Override
    public final List<ItemStack> ftboss$nestedItems() {
        NativeNestedItemFields.requireObserver(ItemContainerContents.class);
        return this.items;
    }
}
