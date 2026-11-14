package ftboss.wear348;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class, remap = false)
public abstract class WearRoundTrip348 {
    @Inject(method = "setDamageValue(I)V", at = @At("RETURN"), require = 1, remap = false)
    private void ftboss$testRoundTrip348(int damage, CallbackInfo callback) {
        ItemStack stack = (ItemStack)(Object)this;
        if (System.getProperties().get("ft348.test.actualStack") != stack) return;
        int current = stack.getDamageValue();
        stack.set(DataComponents.DAMAGE, current + 1);
        stack.set(DataComponents.DAMAGE, current);
        System.getProperties().put("ft348.test.roundTripObserved", Boolean.TRUE);
    }
}
