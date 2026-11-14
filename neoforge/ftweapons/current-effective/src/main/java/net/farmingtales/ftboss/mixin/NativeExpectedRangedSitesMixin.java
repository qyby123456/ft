package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponRanged"},
    remap = false
)
public abstract class NativeExpectedRangedSitesMixin {
    @WrapOperation(
        method = {"prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getPersistentData()Lnet/minecraft/nbt/CompoundTag;",
            remap = false
        )},
        require = 3,
        expect = 3,
        allow = 3,
        remap = false
    )
    private static CompoundTag ftboss$originalRangedPersistentData(ServerPlayer var0, Operation<CompoundTag> var1) {
        return NativeExpectedFieldObservation.persistentData(var0, var1);
    }

    @WrapOperation(
        method = {"prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;",
            remap = false
        )},
        require = 0,
        allow = 2,
        remap = false
    )
    @Group(
        name = "ftboss$rangedCompoundSite",
        min = 2,
        max = 2
    )
    private static CompoundTag ftboss$originalRangedCompoundSrg(CompoundTag var0, String var1, Operation<CompoundTag> var2) {
        return NativeExpectedFieldObservation.compound(var0, var1, var2);
    }

    @WrapOperation(
        method = {"prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;putLong(Ljava/lang/String;J)V",
            remap = false
        )},
        require = 0,
        allow = 2,
        remap = false
    )
    @Group(
        name = "ftboss$rangedLongSite",
        min = 2,
        max = 2
    )
    private static void ftboss$originalRangedLongSrg(CompoundTag var0, String var1, long var2, Operation<Void> var4) {
        NativeExpectedFieldObservation.rangedLong(var0, var1, var2, var4);
    }

    @WrapOperation(
        method = {"saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;",
            remap = false
        )},
        require = 0,
        allow = 1,
        remap = false
    )
    @Group(
        name = "ftboss$rangedRootSite",
        min = 1,
        max = 1
    )
    private static Tag ftboss$originalRangedRootSrg(CompoundTag var0, String var1, Tag var2, Operation<Tag> var3) {
        return NativeExpectedFieldObservation.rangedRoot(var0, var1, var2, var3);
    }
}
