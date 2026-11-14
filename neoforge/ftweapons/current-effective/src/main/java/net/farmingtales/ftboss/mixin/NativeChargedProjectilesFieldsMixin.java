package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.NativeNestedItemFields;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChargedProjectiles.class, remap = false)
public abstract class NativeChargedProjectilesFieldsMixin implements NativeNestedItemFields.Items {
    @Shadow(remap = false)
    @Final
    private List<ItemStack> items;

    @Override
    public final List<ItemStack> ftboss$nestedItems() {
        NativeNestedItemFields.requireObserver(ChargedProjectiles.class);
        return this.items;
    }
}
