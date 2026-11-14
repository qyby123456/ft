package net.farmingtales.ftboss.transaction;

import java.util.Set;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.farmingtales.ftboss.transaction.TransactionWire.TargetOpen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class TransactionAdmission {
    private TransactionAdmission() {
    }

    public static boolean permits(ServerPlayer var0, ItemStack var1, Operation var2) {
        if (!FourWeaponTransactionRuntime.executing(var0.getUUID(), var2)) {
            return false;
        } else {
            Snapshot var3 = FourWeaponTransactionRuntime.current(var0.getUUID());
            return var3 != null
                    && var3.allowed().contains(var2)
                    && var3.binding().owner().equals(var0.getUUID())
                    && FourWeaponFoundationAdapter.read(var1).valid()
                    && var1.getCount() == 1
                    && var0.getInventory().getItem(var3.binding().inventorySlot()) == var1
                    && var0.getItemInHand(var3.binding().hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND) == var1
                    && var3.binding().weapon().equals(FoodWeaponLifecycle.weaponId(var1))
                    && var3.version().cavity() == FourWeaponFoundationAdapter.revision(var1)
                    && var3.version().fingerprint().equals(ForgeTransactionHost.fingerprint(var1, var0.registryAccess()))
                ? var0.isAlive() && !var0.isSpectator()
                : false;
        }
    }

    public static boolean target(ServerPlayer var0, TargetOpen var1, boolean var2) {
        if (var2
            && var0.isAlive()
            && !var0.isSpectator()
            && !var0.isSleeping()
            && !var0.isUsingItem()
            && var0.containerMenu == var0.inventoryMenu
            && var0.inventoryMenu.getCarried().isEmpty()
            && var0.level().dimension().location().toString().equals(var1.dimension())) {
            ItemStack var3 = var0.getItemInHand(var1.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (FourWeaponFoundationAdapter.read(var3).valid()
                && var3.getCount() == 1
                && var1.weapon().equals(FoodWeaponLifecycle.weaponId(var3))
                && var1.revision() == FourWeaponFoundationAdapter.revision(var3)
                && var1.fingerprint().equals(ForgeTransactionHost.fingerprint(var3, var0.registryAccess()))) {
                BlockPos var4 = BlockPos.of(var1.statue());
                ExistingTransactionBoundary var5 = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
                if (var0.level().hasChunkAt(var4)
                    && var0.level().getWorldBorder().isWithinBounds(var4)
                    && var5.statueIdentity(var0, var4) != null
                    && !(var0.distanceToSqr((double)var4.getX() + 0.5, (double)(var4.getY() + 1), (double)var4.getZ() + 0.5) > 64.0)) {
                    ForgeTransactionHost.Policy var6 = var5.policy(var0, var3, var4);
                    return var6 != null && var6.allowed().contains(Operation.OPEN_CONTAINER);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
