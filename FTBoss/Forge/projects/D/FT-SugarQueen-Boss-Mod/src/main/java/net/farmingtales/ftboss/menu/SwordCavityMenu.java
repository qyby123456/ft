package net.farmingtales.ftboss.menu;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.block.BlankStatueContent;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

/** 剑体箱子式容器；2026-10-21 弓弩右键从材料堆装配一件，旧配件安全退回背包，失败则原子回滚。 */
public final class SwordCavityMenu extends AbstractContainerMenu {
    public static final int WIDTH = 196;
    public static final int HEIGHT = 208;
    public static final int BACK_X = 157;
    public static final int BACK_Y = 5;
    public static final int BACK_WIDTH = 29;
    public static final int BACK_HEIGHT = 17;
    public static final int GRID_X = 17;
    public static final int GRID_Y = 44;
    public static final int INVENTORY_Y = 124;
    public static final int HOTBAR_Y = 182;
    public static final int PITCH = 18;
    public static final int BACK_BUTTON = SwordCavityRules.CELL_COUNT;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SimpleContainer cavity = new SimpleContainer(SwordCavityRules.CELL_COUNT);
    private final Inventory inventory;
    private final BlockPos statuePos;
    private final int weaponSlot;
    private final UUID weaponId;
    private final int[] positions;
    private int expectedRevision;
    private final ItemStack boundSource;
    private String expectedFingerprint;
    private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> boundDimension;

    public SwordCavityMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos(), buffer.readVarInt(), buffer.readUUID(), readLayout(buffer));
    }

    public SwordCavityMenu(int containerId, Inventory inventory, BlockPos pos, int weaponSlot, UUID weaponId) {
        this(containerId, inventory, pos, weaponSlot, weaponId, SwordCavityExtensions.layout(inventory.getItem(weaponSlot)));
    }

    public SwordCavityMenu(int containerId, Inventory inventory, BlockPos pos, int weaponSlot, UUID weaponId, int[] positions) {
        super(BlankStatueContent.SWORD_CAVITY_MENU.get(), containerId);
        this.inventory = inventory;
        this.statuePos = pos.immutable();
        this.weaponSlot = weaponSlot;
        this.weaponId = weaponId;
        this.positions = WeaponCavityMenuRules.layout(positions, weapon());
        this.expectedRevision = WeaponCavityMenuRules.revision(weapon());
        List<ItemStack> items = WeaponCavityMenuRules.containerItems(weapon());
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
            cavity.setItem(index, items.get(index).copy());
            addSlot(new CavitySlot(index));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new InventorySlot(column + row * 9 + 9, GRID_X + column * PITCH, INVENTORY_Y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new InventorySlot(column, GRID_X + column * PITCH, HOTBAR_Y));
        }
        boundSource = weapon();
        expectedFingerprint = net.farmingtales.ftboss.transaction.ForgeTransactionHost.fingerprint(boundSource);
        boundDimension = inventory.player.level().dimension();
    }

    private static int[] readLayout(FriendlyByteBuf buffer) {
        int[] positions = new int[SwordCavityRules.CELL_COUNT];
        for (int index = 0; index < positions.length; index++) positions[index] = buffer.readVarInt();
        return SwordCavityExtensions.validateLayout(positions);
    }

    public ItemStack weapon() {
        return weaponSlot < 0 || weaponSlot >= inventory.getContainerSize() ? ItemStack.EMPTY : inventory.getItem(weaponSlot);
    }

    public boolean matches(BlockPos pos) { return statuePos.equals(pos); }

    public boolean reference(int index) {
        return index >= 0 && index < SwordCavityRules.CELL_COUNT && cavity.getItem(index).isEmpty()
                && WeaponCavityMenuRules.reference(weapon(), index);
    }

    private List<ItemStack> contents() {
        List<ItemStack> items = new ArrayList<>();
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) items.add(cavity.getItem(index).copy());
        return items;
    }

    private boolean accepts(int index, ItemStack item) {
        if (!WeaponCavityMenuRules.containerEditable(weapon())) return false;
        List<ItemStack> items = contents();
        ItemStack piece = item.copy();
        if (!piece.isEmpty()) piece.setCount(1);
        items.set(index, piece);
        return WeaponCavityMenuRules.canSetContainerItems(weapon(), items);
    }

    private boolean commit(ServerPlayer player) {
        ItemStack weapon = weapon();
        CompoundTag before = weapon.getTag() == null ? null : weapon.getTag().copy();
        int beforeRevision = expectedRevision;
        try {
            if (!WeaponCavityMenuRules.setContainerItems(player, weapon, contents(), expectedRevision)) return false;
            expectedRevision = WeaponCavityMenuRules.revision(weapon);
            player.inventoryMenu.broadcastChanges();
            return true;
        } catch (RuntimeException failure) {
            weapon.setTag(before);
            expectedRevision = beforeRevision;
            transferFailure(player, failure);
            return false;
        }
    }

    private static void transferFailure(ServerPlayer player, RuntimeException failure) {
        try {
            LOGGER.error("[FTBoss][SwordCavity] Material transfer rolled back", failure);
            player.displayClientMessage(Component.translatable("message.ftboss.sword.transfer_failed"), true);
        } catch (RuntimeException ignored) {
        }
    }

    private List<ItemStack> inventoryContents() {
        List<ItemStack> items = new ArrayList<>();
        for (int index = 0; index < inventory.getContainerSize(); index++) items.add(inventory.getItem(index).copy());
        return items;
    }

    private void synchronize(ServerPlayer player) {
        try {
            player.inventoryMenu.broadcastChanges();
        } catch (RuntimeException failure) {
            transferFailure(player, failure);
        }
        try {
            broadcastFullState();
        } catch (RuntimeException failure) {
            transferFailure(player, failure);
        }
    }

    private void restoreTransfer(ServerPlayer player, List<ItemStack> before, List<ItemStack> beforeInventory, ItemStack beforeCursor) {
        for (int index = 0; index < before.size(); index++) cavity.setItem(index, before.get(index));
        for (int index = 0; index < beforeInventory.size(); index++) if (index != weaponSlot) inventory.setItem(index, beforeInventory.get(index));
        setCarried(beforeCursor);
        synchronize(player);
    }

    @Override
    public boolean stillValid(Player player) {
        if (player != inventory.player || !player.isAlive() || player.isSpectator() || player.isSleeping() || player.isUsingItem()) return false;
        if (player.level().isClientSide) return true;
        ItemStack weapon = weapon();
        return WeaponCavityMenuRules.applies(weapon) && weapon.getCount() == 1 && weaponId.equals(FoodWeaponLifecycle.weaponId(weapon))
                && expectedRevision == WeaponCavityMenuRules.revision(weapon) && player.level().hasChunkAt(statuePos)
                && player.level().getBlockState(statuePos).getBlock() instanceof CandyStatueBlock
                && player.distanceToSqr(statuePos.getX() + 0.5, statuePos.getY() + 1.0, statuePos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType type, Player player) {
        if (type == ClickType.SWAP && button == weaponSlot) return;
        if (slotIndex >= SwordCavityRules.CELL_COUNT && slotIndex < slots.size() && slots.get(slotIndex).getContainerSlot() == weaponSlot) return;
        if (!(player instanceof ServerPlayer serverPlayer)) {
            super.clicked(slotIndex, button, type, player);
            return;
        }
        if (!stillValid(player)) {
            serverPlayer.closeContainer();
            return;
        }
        if (type == ClickType.PICKUP && button == 1 && slotIndex >= 0 && slotIndex < SwordCavityRules.CELL_COUNT
                && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon())
                && (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.matchesType(weapon(), "bow")
                || net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.matchesType(weapon(), "crossbow"))
                && replaceFromCursor(serverPlayer, slotIndex)) return;
        if (type == ClickType.SWAP && slotIndex >= 0 && slotIndex < SwordCavityRules.CELL_COUNT) {
            swapFromInventory(serverPlayer, slotIndex, button);
            return;
        }
        if (type == ClickType.THROW && slotIndex >= 0 && slotIndex < SwordCavityRules.CELL_COUNT) {
            Slot slot = slots.get(slotIndex);
            if (!getCarried().isEmpty() || !slot.hasItem() || !slot.mayPickup(player)) return;
            ItemStack item = cavity.removeItem(slotIndex, 1);
            if (commit(serverPlayer)) {
                slot.onTake(player, item);
                player.drop(item, true);
            } else cavity.setItem(slotIndex, item);
            synchronize(serverPlayer);
            return;
        }
        List<ItemStack> before = contents();
        List<ItemStack> beforeInventory = inventoryContents();
        ItemStack beforeCursor = getCarried().copy();
        try {
            super.clicked(slotIndex, button, type, player);
        } catch (RuntimeException failure) {
            restoreTransfer(serverPlayer, before, beforeInventory, beforeCursor);
            transferFailure(serverPlayer, failure);
            return;
        }
        boolean changed = false;
        for (int index = 0; index < before.size(); index++) {
            ItemStack previous = before.get(index);
            ItemStack current = cavity.getItem(index);
            if (previous.getCount() != current.getCount() || !ItemStack.isSameItemSameTags(previous, current)) changed = true;
        }
        if (changed && !commit(serverPlayer)) {
            restoreTransfer(serverPlayer, before, beforeInventory, beforeCursor);
        }
    }

    private boolean replaceFromCursor(ServerPlayer player, int index) {
        ItemStack incoming = getCarried();
        ItemStack outgoing = cavity.getItem(index);
        if (incoming.getCount() <= 1 || outgoing.isEmpty() || ItemStack.isSameItemSameTags(incoming, outgoing)) return false;
        Slot slot = slots.get(index);
        if (!slot.mayPlace(incoming) || !slot.mayPickup(player)) return true;
        int refundSlot = -1;
        for (int inventorySlot = 0; inventorySlot < inventory.items.size(); inventorySlot++) {
            if (inventorySlot == weaponSlot) continue;
            ItemStack existing = inventory.getItem(inventorySlot);
            if (existing.isEmpty() || ItemStack.isSameItemSameTags(existing, outgoing)
                    && existing.getCount() + outgoing.getCount() <= Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize())) {
                refundSlot = inventorySlot;
                break;
            }
        }
        if (refundSlot < 0) return true;
        List<ItemStack> before = contents();
        List<ItemStack> beforeInventory = inventoryContents();
        ItemStack beforeCursor = incoming.copy();
        try {
            ItemStack installed = incoming.copy();
            installed.setCount(1);
            ItemStack existing = inventory.getItem(refundSlot);
            if (existing.isEmpty()) inventory.setItem(refundSlot, outgoing.copy());
            else existing.grow(outgoing.getCount());
            cavity.setItem(index, installed);
            incoming.shrink(1);
            if (incoming.isEmpty()) setCarried(ItemStack.EMPTY);
            if (!commit(player)) restoreTransfer(player, before, beforeInventory, beforeCursor);
        } catch (RuntimeException failure) {
            restoreTransfer(player, before, beforeInventory, beforeCursor);
            transferFailure(player, failure);
        }
        return true;
    }

    private void swapFromInventory(ServerPlayer player, int index, int button) {
        if (button < 0 || button > 8 && button != 40 || button >= inventory.getContainerSize() || button == weaponSlot) return;
        Slot slot = slots.get(index);
        ItemStack incoming = inventory.getItem(button);
        ItemStack outgoing = cavity.getItem(index).copy();
        if (incoming.isEmpty() && outgoing.isEmpty() || !outgoing.isEmpty() && !slot.mayPickup(player)
                || !incoming.isEmpty() && !slot.mayPlace(incoming)) return;
        List<ItemStack> before = contents();
        List<ItemStack> beforeInventory = inventoryContents();
        ItemStack beforeCursor = getCarried().copy();
        ItemStack remainder = ItemStack.EMPTY;
        try {
            ItemStack installed = incoming.copy();
            if (!installed.isEmpty()) installed.setCount(1);
            cavity.setItem(index, installed);
            if (incoming.isEmpty() || incoming.getCount() == 1) inventory.setItem(button, outgoing.copy());
            else {
                incoming.shrink(1);
                remainder = outgoing.copy();
                if (!remainder.isEmpty()) inventory.add(remainder);
            }
        } catch (RuntimeException failure) {
            restoreTransfer(player, before, beforeInventory, beforeCursor);
            transferFailure(player, failure);
            return;
        }
        if (commit(player)) {
            if (!outgoing.isEmpty()) slot.onTake(player, outgoing);
            if (!remainder.isEmpty()) player.drop(remainder, true);
        } else {
            restoreTransfer(player, before, beforeInventory, beforeCursor);
            return;
        }
        synchronize(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack result = original.copy();
        if (slotIndex < SwordCavityRules.CELL_COUNT) {
            if (!moveItemStackTo(original, SwordCavityRules.CELL_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(original, 0, SwordCavityRules.CELL_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, original);
        return result;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) return false;
        if (button == BACK_BUTTON) {
            serverPlayer.closeContainer();
            StatueMenuNetwork.open(serverPlayer, statuePos);
            return true;
        }
        SwordCavityRules.Slot slot = SwordCavityRules.Slot.byIndex(button);
        if (slot == null || !getCarried().isEmpty() || !reference(button)) return false;
        ItemStack weapon = weapon();
        CompoundTag before = weapon.getTag() == null ? null : weapon.getTag().copy();
        int beforeRevision = expectedRevision;
        try {
            if (!WeaponCavityMenuRules.clearReference(serverPlayer, weapon, slot, expectedRevision)) return false;
            expectedRevision = WeaponCavityMenuRules.revision(weapon);
            serverPlayer.inventoryMenu.broadcastChanges();
            broadcastChanges();
            return true;
        } catch (RuntimeException failure) {
            weapon.setTag(before);
            expectedRevision = beforeRevision;
            transferFailure(serverPlayer, failure);
            synchronize(serverPlayer);
            return false;
        }
    }

    private final class CavitySlot extends Slot {
        private CavitySlot(int index) {
            super(cavity, index, GRID_X + positions[index] % SwordCavityRules.COLUMNS * PITCH,
                    GRID_Y + positions[index] / SwordCavityRules.COLUMNS * PITCH);
        }

        @Override
        public int getMaxStackSize() { return 1; }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return WeaponCavityMenuRules.cellUnlocked(weapon(), getContainerSlot()) && accepts(getContainerSlot(), stack);
        }

        @Override
        public boolean mayPickup(Player player) { return player == inventory.player && accepts(getContainerSlot(), ItemStack.EMPTY); }
    }

    private final class InventorySlot extends Slot {
        private InventorySlot(int index, int posX, int posY) { super(inventory, index, posX, posY); }

        @Override
        public boolean mayPlace(ItemStack stack) { return getContainerSlot() != weaponSlot; }

        @Override
        public boolean mayPickup(Player player) { return getContainerSlot() != weaponSlot && player == inventory.player; }
    }
}
