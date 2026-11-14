/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.transaction;

import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FoundationEntryPoints {
    private FoundationEntryPoints() {
    }

    public static boolean seedFromStatue(ServerPlayer serverPlayer, ItemStack itemStack, BlockPos blockPos, boolean bl) {
        ForgeTransactionHost.Policy policy;
        if (!bl || serverPlayer == null || itemStack == null || blockPos == null || !serverPlayer.f_8924_.m_18695_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || serverPlayer.f_36096_ != serverPlayer.f_36095_ || !serverPlayer.f_36095_.m_142621_().m_41619_() || itemStack.m_41613_() != 1 || itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_() || !FourWeaponFoundationAdapter.read(itemStack).valid() || !serverPlayer.m_9236_().m_46805_(blockPos) || !serverPlayer.m_9236_().m_6857_().m_61937_(blockPos) || serverPlayer.m_20275_((double)blockPos.m_123341_() + 0.5, (double)(blockPos.m_123342_() + 1), (double)blockPos.m_123343_() + 0.5) > 64.0) {
            return false;
        }
        ExistingTransactionBoundary existingTransactionBoundary = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        if (existingTransactionBoundary.statueIdentity(serverPlayer, blockPos) == null) {
            return false;
        }
        try {
            policy = existingTransactionBoundary.policy(serverPlayer, itemStack, blockPos);
        }
        catch (RuntimeException runtimeException) {
            return false;
        }
        if (policy == null || !policy.allowed().contains((Object)FourWeaponTransactions.Operation.OPEN_CONTAINER)) {
            return false;
        }
        FoodWeaponLifecycle.ensureTracked(itemStack);
        if (FoodWeaponLifecycle.weaponId(itemStack) == null) {
            CompoundTag compoundTag = itemStack.m_41698_("FTBossWeaponLifecycle");
            if (compoundTag.m_128441_("Weapon")) {
                return false;
            }
            compoundTag.m_128362_("Weapon", UUID.randomUUID());
        }
        return FoodWeaponLifecycle.weaponId(itemStack) != null;
    }

    public static FourWeaponTransactions.Operation legacyOperation(int n) {
        return switch (n) {
            case 5 -> FourWeaponTransactions.Operation.EVOLVE;
            case 9 -> FourWeaponTransactions.Operation.SELECT_SKILL;
            case 13 -> FourWeaponTransactions.Operation.START_REPAIR;
            case 14 -> FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            case 15 -> FourWeaponTransactions.Operation.CLAIM_REPAIR;
            default -> null;
        };
    }

    public static boolean legacyGlobalAction(int n) {
        return n == 0 || n == 1 || n == 4 || n == 8;
    }
}

