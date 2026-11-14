/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={CompoundTag.class}, remap=false)
public abstract class NativeExpectedMapWriteMixin {
    @WrapOperation(method={"m_128365_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "m_128356_(Ljava/lang/String;J)V", "m_128359_(Ljava/lang/String;Ljava/lang/String;)V"}, at={@At(value="INVOKE", target="Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap=false)}, require=3, expect=3, allow=3, remap=false)
    private Object ftboss$originalFieldMapPut(Map<Object, Object> map, Object object, Object object2, Operation<Object> operation) {
        return NativeExpectedFieldObservation.mapPut((CompoundTag)this, map, object, object2, operation);
    }
}

