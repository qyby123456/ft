package net.farmingtales.ftboss.mixin;

import java.lang.StackWalker.Option;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerRevocationNodeAccess;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnergyStorage.class, remap = false)
public abstract class NativeEnergyStorageLifetimeMixin implements OwnerRevocationNodeAccess {
    @Unique
    private OwnerFullRepairGuard.Node ftboss$ownerRevocationNode;

    @Unique
    @Override
    public final OwnerFullRepairGuard.Node ftboss$ownerRevocationNode() {
        return ((Object)this).getClass() == EnergyStorage.class ? this.ftboss$ownerRevocationNode : null;
    }

    @Unique
    @Override
    public final void ftboss$ownerInstallRevocationNode(OwnerFullRepairGuard.Node node) {
        boolean trusted = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst()
                .map(frame -> frame.getDeclaringClass() == OwnerFullRepairGuard.class
                    && frame.getMethodName().equals("node")
                    && frame.getDescriptor().equals("(Ljava/lang/Object;Z)Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Node;"))
                .orElse(false));
        if (trusted && ((Object)this).getClass() == EnergyStorage.class && this.ftboss$ownerRevocationNode == null) {
            this.ftboss$ownerRevocationNode = node;
        }
    }

    @Unique
    private void ftboss$nativeEnergyChanging() {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable observationFailure) {
        }
    }

    @Inject(
        method = {"receiveEnergy(IZ)I", "extractEnergy(IZ)I"},
        at = @At(value = "FIELD", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;energy:I", opcode = Opcodes.PUTFIELD),
        require = 2,
        allow = 2,
        remap = false
    )
    private void ftboss$nativeEnergyTransferWrite(CallbackInfoReturnable<Integer> callback) {
        this.ftboss$nativeEnergyChanging();
    }

    @Inject(
        method = "deserializeNBT(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)V",
        at = @At(value = "FIELD", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;energy:I", opcode = Opcodes.PUTFIELD),
        require = 1,
        allow = 1,
        remap = false
    )
    private void ftboss$nativeEnergyDeserializeWrite(CallbackInfo callback) {
        this.ftboss$nativeEnergyChanging();
    }
}
