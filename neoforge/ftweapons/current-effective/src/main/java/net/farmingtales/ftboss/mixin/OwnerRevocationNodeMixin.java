package net.farmingtales.ftboss.mixin;

import java.lang.StackWalker.Option;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerRevocationNodeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    targets = {"net.neoforged.neoforge.capabilities.ItemCapability", "net.minecraft.nbt.CompoundTag", "net.minecraft.nbt.ListTag", "net.minecraft.nbt.ByteArrayTag", "net.minecraft.nbt.IntArrayTag", "net.minecraft.nbt.LongArrayTag", "net.minecraft.world.item.ItemStack", "net.minecraft.core.component.PatchedDataComponentMap", "net.minecraft.world.item.component.CustomData", "net.minecraft.world.item.component.ItemContainerContents", "net.minecraft.world.item.component.BundleContents", "net.minecraft.world.item.component.ChargedProjectiles", "net.minecraft.world.item.alchemy.PotionContents", "net.minecraft.world.effect.MobEffectInstance", "net.minecraft.world.effect.MobEffectInstance$BlendState"},
    remap = false
)
public abstract class OwnerRevocationNodeMixin implements OwnerRevocationNodeAccess {
    @Unique
    private OwnerFullRepairGuard.Node ftboss$ownerRevocationNode;

    @Unique
    @Override
    public final OwnerFullRepairGuard.Node ftboss$ownerRevocationNode() {
        return this.ftboss$ownerRevocationNode;
    }

    @Unique
    @Override
    public final void ftboss$ownerInstallRevocationNode(OwnerFullRepairGuard.Node var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0 -> var0.skip(1L).findFirst().map(var0x -> var0x.getDeclaringClass() == OwnerFullRepairGuard.class).orElse(false));
        if (var2 && this.ftboss$ownerRevocationNode == null) {
            this.ftboss$ownerRevocationNode = var1;
        }
    }
}
