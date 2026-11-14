package net.farmingtales.ftboss.transaction;

import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class FoundationEntryPoints {
    private FoundationEntryPoints() {
    }

    public static boolean seedFromStatue(ServerPlayer var0, ItemStack var1, BlockPos var2, boolean var3) {
        if (var3
            && var0 != null
            && var1 != null
            && var2 != null
            && var0.server.isSameThread()
            && var0.isAlive()
            && !var0.isSpectator()
            && !var0.isSleeping()
            && !var0.isUsingItem()
            && var0.containerMenu == var0.inventoryMenu
            && var0.inventoryMenu.getCarried().isEmpty()
            && var1.getCount() == 1
            && (var1 == var0.getMainHandItem() || var1 == var0.getOffhandItem())
            && FourWeaponFoundationAdapter.read(var1).valid()
            && var0.level().hasChunkAt(var2)
            && var0.level().getWorldBorder().isWithinBounds(var2)
            && !(var0.distanceToSqr((double)var2.getX() + 0.5, (double)(var2.getY() + 1), (double)var2.getZ() + 0.5) > 64.0)) {
            ExistingTransactionBoundary var4 = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
            if (var4.statueIdentity(var0, var2) == null) {
                return false;
            } else {
                ForgeTransactionHost.Policy var5;
                try {
                    var5 = var4.policy(var0, var1, var2);
                } catch (RuntimeException var7) {
                    return false;
                }

                if (var5 != null && var5.allowed().contains(Operation.OPEN_CONTAINER)) {
                    FoodWeaponLifecycle.ensureTracked(var1);
                    if (FoodWeaponLifecycle.weaponId(var1) == null) {
                        CompoundTag var6 = nativeLifecycle(var1);
                        if (var6.contains("Weapon")) {
                            return false;
                        }

                        var6.putUUID("Weapon", UUID.randomUUID());
                    }

                    return FoodWeaponLifecycle.weaponId(var1) != null;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }


    private static CompoundTag nativeLifecycle(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data == CustomData.EMPTY) {
            data = CustomData.of(new CompoundTag());
            stack.set(DataComponents.CUSTOM_DATA, data);
            if (stack.get(DataComponents.CUSTOM_DATA) != data) {
                throw new IllegalStateException("Native statue lifecycle component replaced during creation");
            }
        }
        CompoundTag root = data.getUnsafe();
        if (!root.contains("FTBossWeaponLifecycle", 10)) {
            root.put("FTBossWeaponLifecycle", new CompoundTag());
        }
        return root.getCompound("FTBossWeaponLifecycle");
    }

    public static Operation legacyOperation(int var0) {
        return switch (var0) {
            case 5 -> Operation.EVOLVE;
            default -> null;
            case 9 -> Operation.SELECT_SKILL;
            case 13 -> Operation.START_REPAIR;
            case 14 -> Operation.SUBMIT_REPAIR;
            case 15 -> Operation.CLAIM_REPAIR;
        };
    }

    public static boolean legacyGlobalAction(int var0) {
        return var0 == 0 || var0 == 1 || var0 == 4 || var0 == 8;
    }
}
