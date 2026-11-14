package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
    value = {CompoundTag.class},
    remap = false
)
public abstract class NativeExpectedMapWriteMixin {
    @WrapOperation(
        method = {"put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "putLong(Ljava/lang/String;J)V", "putString(Ljava/lang/String;Ljava/lang/String;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            remap = false
        )},
        require = 3,
        expect = 3,
        allow = 3,
        remap = false
    )
    private Object ftboss$originalFieldMapPut(Map<Object, Object> var1, Object var2, Object var3, Operation<Object> var4) {
        return NativeExpectedFieldObservation.mapPut((CompoundTag)(Object)this, var1, var2, var3, var4);
    }
}
