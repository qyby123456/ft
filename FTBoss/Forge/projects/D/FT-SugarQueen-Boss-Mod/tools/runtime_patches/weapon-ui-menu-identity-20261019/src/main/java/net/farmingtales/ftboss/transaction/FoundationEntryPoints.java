package net.farmingtales.ftboss.transaction;

import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FoundationEntryPoints {
    private FoundationEntryPoints() {}
    public static boolean seedFromStatue(ServerPlayer player, ItemStack stack, BlockPos pos, boolean negotiated) {
        if (!negotiated || player == null || stack == null || pos == null || !player.server.isSameThread() || !player.isAlive() || player.isSpectator() || player.isSleeping() || player.isUsingItem()
                || player.containerMenu != player.inventoryMenu || !player.inventoryMenu.getCarried().isEmpty()
                || stack.getCount() != 1 || stack != player.getMainHandItem() && stack != player.getOffhandItem()
                || !FourWeaponFoundationAdapter.read(stack).valid() || !player.level().hasChunkAt(pos)
                || !player.level().getWorldBorder().isWithinBounds(pos)
                || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5) > 64) return false;
        var boundary = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        if (boundary.statueIdentity(player, pos) == null) return false;
        ForgeTransactionHost.Policy policy;
        try { policy = boundary.policy(player, stack, pos); }
        catch (RuntimeException invalidRules) { return false; }
        if (policy == null || !policy.allowed().contains(FourWeaponTransactions.Operation.OPEN_CONTAINER)) return false;
        FoodWeaponLifecycle.ensureTracked(stack);
        if (FoodWeaponLifecycle.weaponId(stack) == null) {
            var state = stack.getOrCreateTagElement(FoodWeaponLifecycle.ROOT);
            if (state.contains("Weapon")) return false;
            state.putUUID("Weapon", UUID.randomUUID());
        }
        return FoodWeaponLifecycle.weaponId(stack) != null;
    }
    public static FourWeaponTransactions.Operation legacyOperation(int action) {
        return switch (action) {
            case 5 -> FourWeaponTransactions.Operation.EVOLVE;
            case 9 -> FourWeaponTransactions.Operation.SELECT_SKILL;
            case 13 -> FourWeaponTransactions.Operation.START_REPAIR;
            case 14 -> FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            case 15 -> FourWeaponTransactions.Operation.CLAIM_REPAIR;
            default -> null;
        };
    }
    public static boolean legacyGlobalAction(int action) { return action == 0 || action == 1 || action == 4 || action == 8; }
}
