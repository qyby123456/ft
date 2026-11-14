package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.StackWalker.Option;
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

@Mixin({Entity.class})
public abstract class OwnerFieldExpectedEntityMixin implements OwnerFieldEntityAccess {
    @Shadow
    private CompoundTag persistentData;
    @Shadow
    private Level level;
    @Unique
    private OwnerFieldExpectedAuthority.DomainBefore ftboss$ownerFieldDomain;

    @Unique
    @Override
    public final OwnerFieldExpectedAuthority.DomainBefore ftboss$ownerFieldDomain() {
        return this.ftboss$ownerFieldDomain;
    }

    @Unique
    @Override
    public final void ftboss$ownerFieldInstallDomain(OwnerFieldExpectedAuthority.DomainBefore var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0 -> var0.skip(1L).findFirst().map(var0x -> var0x.getDeclaringClass() == OwnerFieldExpectedAuthority.class).orElse(false));
        if (var2) {
            this.ftboss$ownerFieldDomain = var1;
        }
    }

    @Unique
    @Override
    public final Level ftboss$ownerFieldRawLevel() {
        return this.level;
    }

    @Inject(
        method = {"<init>"},
        at = {@At("RETURN")},
        require = 1
    )
    private void ftboss$ownerFieldConstructed(CallbackInfo var1) {
        try {
            OwnerFieldExpectedAuthority.constructed((Entity)(Object)this, this.persistentData);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"load(Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$ownerFieldLoadQuoted(CompoundTag var1, CallbackInfo var2) {
        try {
            OwnerFieldExpectedAuthority.loadQuoted((Entity)(Object)this, var1);
        } catch (Throwable var6) {
            try {
                OwnerFieldExpectedAuthority.changed(this);
            } catch (Throwable var5) {
            }
        }
    }

    @WrapOperation(
        method = {"load(Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;persistentData:Lnet/minecraft/nbt/CompoundTag;",
            opcode = 181,
            remap = false
        )},
        require = 1
    )
    private void ftboss$ownerFieldLoadAssigned(Entity var1, CompoundTag var2, Operation<Void> var3) {
        var3.call(new Object[]{var1, var2});

        try {
            OwnerFieldExpectedAuthority.loadAssigned(var1, var2);
        } catch (Throwable var7) {
            try {
                OwnerFieldExpectedAuthority.changed(var1);
            } catch (Throwable var6) {
            }
        }
    }

    @Inject(
        method = {"getPersistentData()Lnet/minecraft/nbt/CompoundTag;"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$ownerFieldPersistentExposed(CallbackInfoReturnable<CompoundTag> var1) {
        try {
            OwnerFieldExpectedAuthority.persistentExposed((Entity)(Object)this, var1.getReturnValue());
        } catch (Throwable var3) {
        }
    }
}
