/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerFieldEntityAccess;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public abstract class OwnerFieldExpectedEntityMixin
implements OwnerFieldEntityAccess {
    @Shadow
    private CompoundTag persistentData;
    @Shadow
    private Level f_19853_;
    @Unique
    private OwnerFieldExpectedAuthority.DomainBefore ftboss$ownerFieldDomain;

    @Override
    @Unique
    public final OwnerFieldExpectedAuthority.DomainBefore ftboss$ownerFieldDomain() {
        return this.ftboss$ownerFieldDomain;
    }

    @Override
    @Unique
    public final void ftboss$ownerFieldInstallDomain(OwnerFieldExpectedAuthority.DomainBefore domainBefore) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFieldExpectedAuthority.class).orElse(false));
        if (bl) {
            this.ftboss$ownerFieldDomain = domainBefore;
        }
    }

    @Override
    @Unique
    public final Level ftboss$ownerFieldRawLevel() {
        return this.f_19853_;
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")}, require=1)
    private void ftboss$ownerFieldConstructed(CallbackInfo callbackInfo) {
        try {
            OwnerFieldExpectedAuthority.constructed((Entity)this, this.persistentData);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"load(Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$ownerFieldLoadQuoted(CompoundTag compoundTag, CallbackInfo callbackInfo) {
        try {
            OwnerFieldExpectedAuthority.loadQuoted((Entity)this, compoundTag);
        }
        catch (Throwable throwable) {
            try {
                OwnerFieldExpectedAuthority.changed(this);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
    }

    @WrapOperation(method={"load(Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="FIELD", target="Lnet/minecraft/world/entity/Entity;persistentData:Lnet/minecraft/nbt/CompoundTag;", opcode=181, remap=false)}, require=1)
    private void ftboss$ownerFieldLoadAssigned(Entity entity, CompoundTag compoundTag, Operation<Void> operation) {
        operation.call(new Object[]{entity, compoundTag});
        try {
            OwnerFieldExpectedAuthority.loadAssigned(entity, compoundTag);
        }
        catch (Throwable throwable) {
            try {
                OwnerFieldExpectedAuthority.changed(entity);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
    }

    @Inject(method={"getPersistentData()Lnet/minecraft/nbt/CompoundTag;"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$ownerFieldPersistentExposed(CallbackInfoReturnable<CompoundTag> callbackInfoReturnable) {
        try {
            OwnerFieldExpectedAuthority.persistentExposed((Entity)this, callbackInfoReturnable.getReturnValue());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

