package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.NativeNestedItemFields;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BundleContents.class, remap = false)
public abstract class NativeBundleContentsFieldsMixin implements NativeNestedItemFields.Items {
    @Shadow(remap = false)
    @Final
    private List<ItemStack> items;

    @Override
    public final List<ItemStack> ftboss$nestedItems() {
        NativeNestedItemFields.requireObserver(BundleContents.class);
        return this.items;
    }
}
