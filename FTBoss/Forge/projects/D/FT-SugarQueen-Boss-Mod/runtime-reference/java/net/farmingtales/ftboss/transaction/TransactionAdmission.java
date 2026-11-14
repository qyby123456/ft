/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.transaction;

import java.util.Set;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionWire;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class TransactionAdmission {
    private TransactionAdmission() {
    }

    public static boolean permits(ServerPlayer serverPlayer, ItemStack itemStack, FourWeaponTransactions.Operation operation) {
        if (!FourWeaponTransactionRuntime.executing(serverPlayer.m_20148_(), operation)) {
            return false;
        }
        FourWeaponTransactions.Snapshot snapshot = FourWeaponTransactionRuntime.current(serverPlayer.m_20148_());
        if (!(snapshot != null && snapshot.allowed().contains((Object)operation) && snapshot.binding().owner().equals(serverPlayer.m_20148_()) && FourWeaponFoundationAdapter.read(itemStack).valid() && itemStack.m_41613_() == 1 && serverPlayer.m_150109_().m_8020_(snapshot.binding().inventorySlot()) == itemStack && serverPlayer.m_21120_(snapshot.binding().hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND) == itemStack && snapshot.binding().weapon().equals(FoodWeaponLifecycle.weaponId(itemStack)) && snapshot.version().cavity() == FourWeaponFoundationAdapter.revision(itemStack) && snapshot.version().fingerprint().equals(ForgeTransactionHost.fingerprint(itemStack)))) {
            return false;
        }
        return serverPlayer.m_6084_() && !serverPlayer.m_5833_();
    }

    public static boolean target(ServerPlayer serverPlayer, TransactionWire.TargetOpen targetOpen, boolean bl) {
        if (!bl || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || serverPlayer.f_36096_ != serverPlayer.f_36095_ || !serverPlayer.f_36095_.m_142621_().m_41619_() || !serverPlayer.m_9236_().m_46472_().m_135782_().toString().equals(targetOpen.dimension())) {
            return false;
        }
        ItemStack itemStack = serverPlayer.m_21120_(targetOpen.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!(FourWeaponFoundationAdapter.read(itemStack).valid() && itemStack.m_41613_() == 1 && targetOpen.weapon().equals(FoodWeaponLifecycle.weaponId(itemStack)) && targetOpen.revision() == FourWeaponFoundationAdapter.revision(itemStack) && targetOpen.fingerprint().equals(ForgeTransactionHost.fingerprint(itemStack)))) {
            return false;
        }
        BlockPos blockPos = BlockPos.m_122022_((long)targetOpen.statue());
        ExistingTransactionBoundary existingTransactionBoundary = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        if (!serverPlayer.m_9236_().m_46805_(blockPos) || !serverPlayer.m_9236_().m_6857_().m_61937_(blockPos) || existingTransactionBoundary.statueIdentity(serverPlayer, blockPos) == null || serverPlayer.m_20275_((double)blockPos.m_123341_() + 0.5, (double)(blockPos.m_123342_() + 1), (double)blockPos.m_123343_() + 0.5) > 64.0) {
            return false;
        }
        ForgeTransactionHost.Policy policy = existingTransactionBoundary.policy(serverPlayer, itemStack, blockPos);
        return policy != null && policy.allowed().contains((Object)FourWeaponTransactions.Operation.OPEN_CONTAINER);
    }
}

