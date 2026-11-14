package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativePreQueryCapabilityLifetime199;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "top.theillusivec4.curios.mixin.CuriosImplMixinHooks", remap = false)
public abstract class NativeCuriosProviderStateMixin257 {
    @org.spongepowered.asm.mixin.injection.Redirect(method = "<clinit>",
        at = @At(value = "NEW", target = "java/util/concurrent/ConcurrentHashMap"), require = 1, allow = 1, remap = false)
    private static java.util.concurrent.ConcurrentHashMap<Object, Object> ftboss$trackedOriginalRegistry() {
        return net.farmingtales.ftboss.item.NativeCuriosProviderFields260.originalRegistry();
    }

    @Inject(method = "registerCurio(Lnet/minecraft/world/item/Item;Ltop/theillusivec4/curios/api/type/capability/ICurioItem;)V", at = @At("HEAD"), require = 1, allow = 1, remap = false)
    private static void ftboss$nativeCuriosStateBefore(Item item, @Coerce Object provider, CallbackInfo callback) {
        try {
            NativePreQueryCapabilityLifetime199.curiosState(item, true);
        } catch (Throwable observationFailure) {
        }
    }

    @Inject(method = "registerCurio(Lnet/minecraft/world/item/Item;Ltop/theillusivec4/curios/api/type/capability/ICurioItem;)V", at = @At("RETURN"), require = 1, allow = 1, remap = false)
    private static void ftboss$nativeCuriosStateAfter(Item item, @Coerce Object provider, CallbackInfo callback) {
        try {
            NativePreQueryCapabilityLifetime199.curiosState(item, false);
        } catch (Throwable observationFailure) {
        }
    }
    @org.spongepowered.asm.mixin.injection.Redirect(
        method = "registerCurio(Lnet/minecraft/world/item/Item;Ltop/theillusivec4/curios/api/type/capability/ICurioItem;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
        require = 1, allow = 1, remap = false)
    private static Object ftboss$nativeCuriosStateWrite(java.util.Map<Object, Object> registry, Object key, Object requested) throws Throwable {
        Object token = null;
        try {
            token = net.farmingtales.ftboss.item.NativeCuriosProviderFields260.before(registry, key, requested);
        } catch (Throwable observationFailure) { }
        Object returned = null;
        Throwable originalFailure = null;
        try {
            returned = registry.put(key, requested);
            return returned;
        } catch (Throwable failure) {
            originalFailure = failure;
            throw failure;
        } finally {
            try {
                net.farmingtales.ftboss.item.NativeCuriosProviderFields260.after(token, registry, key, requested, returned, originalFailure);
            } catch (Throwable observationFailure) { }
        }
    }

}
