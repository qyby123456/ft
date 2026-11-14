package net.farmingtales.ftboss.menu;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.block.BlankStatueContent;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeStackComponents;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.TransferInvariant;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.farmingtales.ftboss.transaction.TransferInvariant.Contents;
import net.farmingtales.ftboss.transaction.TransferInvariant.Stack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

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
    public static final int BACK_BUTTON = 27;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SimpleContainer cavity = new SimpleContainer(27);
    private final Inventory inventory;
    private final BlockPos statuePos;
    private final int weaponSlot;
    private final UUID weaponId;
    private final int[] positions;
    private int expectedRevision;
    private UUID transactionSession;
    private Binding transactionBinding;
    private boolean transferRejected;
    private boolean authorizedClick;
    private final ItemStack boundSource;
    private String expectedFingerprint;
    private final ResourceKey<Level> boundDimension;

    public SwordCavityMenu(int var1, Inventory var2, FriendlyByteBuf var3) {
        this(var1, var2, var3.readBlockPos(), var3.readVarInt(), var3.readUUID(), readLayout(var3));
    }

    public SwordCavityMenu(int var1, Inventory var2, BlockPos var3, int var4, UUID var5) {
        this(var1, var2, var3, var4, var5, SwordCavityExtensions.layout(var2.getItem(var4)));
    }

    public SwordCavityMenu(int var1, Inventory var2, BlockPos var3, int var4, UUID var5, int[] var6) {
        super((MenuType<?>)BlankStatueContent.SWORD_CAVITY_MENU.get(), var1);
        this.inventory = var2;
        this.statuePos = var3.immutable();
        this.weaponSlot = var4;
        this.weaponId = var5;
        this.positions = WeaponCavityMenuRules.layout(var6, this.weapon());
        this.expectedRevision = WeaponCavityMenuRules.revision(this.weapon());
        List var7 = WeaponCavityMenuRules.containerItems(this.weapon());

        for (int var8 = 0; var8 < 27; var8++) {
            this.cavity.setItem(var8, ((ItemStack)var7.get(var8)).copy());
            this.addSlot(new SwordCavityMenu.CavitySlot(var8));
        }

        for (int var10 = 0; var10 < 3; var10++) {
            for (int var9 = 0; var9 < 9; var9++) {
                this.addSlot(new SwordCavityMenu.InventorySlot(var9 + var10 * 9 + 9, 17 + var9 * 18, 124 + var10 * 18));
            }
        }

        for (int var11 = 0; var11 < 9; var11++) {
            this.addSlot(new SwordCavityMenu.InventorySlot(var11, 17 + var11 * 18, 182));
        }

        this.boundSource = this.weapon();
        this.expectedFingerprint = ForgeTransactionHost.fingerprint(this.boundSource, this.inventory.player.registryAccess());
        this.boundDimension = this.inventory.player.level().dimension();
    }

    private static int[] readLayout(FriendlyByteBuf var0) {
        int[] var1 = new int[27];

        for (int var2 = 0; var2 < var1.length; var2++) {
            var1[var2] = var0.readVarInt();
        }

        return SwordCavityExtensions.validateLayout(var1);
    }

    public ItemStack weapon() {
        return this.weaponSlot >= 0 && this.weaponSlot < this.inventory.getContainerSize() ? this.inventory.getItem(this.weaponSlot) : ItemStack.EMPTY;
    }

    public boolean matches(BlockPos var1) {
        return this.statuePos.equals(var1);
    }

    public boolean reference(int var1) {
        return var1 >= 0 && var1 < 27 && this.cavity.getItem(var1).isEmpty() ? WeaponCavityMenuRules.reference(this.weapon(), var1) : false;
    }

    private List<ItemStack> contents() {
        ArrayList var1 = new ArrayList();

        for (int var2 = 0; var2 < 27; var2++) {
            var1.add(this.cavity.getItem(var2).copy());
        }

        return var1;
    }

    private boolean accepts(int var1, ItemStack var2) {
        if (!WeaponCavityMenuRules.containerEditable(this.weapon())) {
            return false;
        } else {
            List var3 = this.contents();
            ItemStack var4 = var2.copy();
            if (!var4.isEmpty()) {
                var4.setCount(1);
            }

            var3.set(var1, var4);
            return WeaponCavityMenuRules.canSetContainerItems(this.weapon(), var3);
        }
    }

    private boolean commit(ServerPlayer var1) {
        ItemStack var2 = this.weapon();
        NativeStackComponents.Snapshot var3 = NativeStackComponents.capture(var2);
        int var4 = this.expectedRevision;

        try {
            if (!WeaponCavityMenuRules.setContainerItems(var1, var2, this.contents(), this.expectedRevision)) {
                return false;
            } else {
                this.expectedRevision = WeaponCavityMenuRules.revision(var2);
                var1.inventoryMenu.broadcastChanges();
                return true;
            }
        } catch (RuntimeException var6) {
            var3.restore(var2);
            this.expectedRevision = var4;
            transferFailure(var1, var6);
            return false;
        }
    }

    private static void transferFailure(ServerPlayer var0, RuntimeException var1) {
        try {
            LOGGER.error("[FTBoss][SwordCavity] Material transfer rolled back", var1);
            var0.displayClientMessage(Component.translatable("message.ftboss.sword.transfer_failed"), true);
        } catch (RuntimeException var3) {
        }
    }

    private List<ItemStack> inventoryContents() {
        ArrayList var1 = new ArrayList();

        for (int var2 = 0; var2 < this.inventory.getContainerSize(); var2++) {
            var1.add(this.inventory.getItem(var2).copy());
        }

        return var1;
    }

    private void synchronize(ServerPlayer var1) {
        try {
            var1.inventoryMenu.broadcastChanges();
        } catch (RuntimeException var4) {
            transferFailure(var1, var4);
        }

        try {
            this.broadcastFullState();
        } catch (RuntimeException var3) {
            transferFailure(var1, var3);
        }
    }

    private void restoreTransfer(ServerPlayer var1, List<ItemStack> var2, List<ItemStack> var3, ItemStack var4) {
        for (int var5 = 0; var5 < var2.size(); var5++) {
            this.cavity.setItem(var5, (ItemStack)var2.get(var5));
        }

        for (int var6 = 0; var6 < var3.size(); var6++) {
            if (var6 != this.weaponSlot) {
                this.inventory.setItem(var6, (ItemStack)var3.get(var6));
            }
        }

        this.setCarried(var4);
        this.synchronize(var1);
    }

    @Override
    public boolean stillValid(Player var1) {
        if (var1 != this.inventory.player || !var1.isAlive() || var1.isSpectator() || var1.isSleeping() || var1.isUsingItem()) {
            return false;
        } else if (var1.level().isClientSide) {
            return true;
        } else {
            ItemStack var2 = this.weapon();
            return WeaponCavityMenuRules.applies(var2)
                && var2.getCount() == 1
                && this.weaponId.equals(FoodWeaponLifecycle.weaponId(var2))
                && this.expectedRevision == WeaponCavityMenuRules.revision(var2)
                && var1.level().hasChunkAt(this.statuePos)
                && var1.level().getBlockState(this.statuePos).getBlock() instanceof CandyStatueBlock
                && var1.distanceToSqr((double)this.statuePos.getX() + 0.5, (double)this.statuePos.getY() + 1.0, (double)this.statuePos.getZ() + 0.5) <= 64.0;
        }
    }

    private void swapFromInventory(ServerPlayer var1, int var2, int var3) {
        if (var3 >= 0 && (var3 <= 8 || var3 == 40) && var3 < this.inventory.getContainerSize() && var3 != this.weaponSlot) {
            Slot var4 = this.slots.get(var2);
            ItemStack var5 = this.inventory.getItem(var3);
            ItemStack var6 = this.cavity.getItem(var2).copy();
            if ((!var5.isEmpty() || !var6.isEmpty()) && (var6.isEmpty() || var4.mayPickup(var1)) && (var5.isEmpty() || var4.mayPlace(var5))) {
                List var7 = this.contents();
                List var8 = this.inventoryContents();
                ItemStack var9 = this.getCarried().copy();
                ItemStack var10 = ItemStack.EMPTY;

                try {
                    ItemStack var11 = var5.copy();
                    if (!var11.isEmpty()) {
                        var11.setCount(1);
                    }

                    this.cavity.setItem(var2, var11);
                    if (!var5.isEmpty() && var5.getCount() != 1) {
                        var5.shrink(1);
                        var10 = var6.copy();
                        if (!var10.isEmpty()) {
                            this.inventory.add(var10);
                        }
                    } else {
                        this.inventory.setItem(var3, var6.copy());
                    }
                } catch (RuntimeException var12) {
                    this.restoreTransfer(var1, var7, var8, var9);
                    transferFailure(var1, var12);
                    return;
                }

                if (this.commit(var1)) {
                    if (!var6.isEmpty()) {
                        var4.onTake(var1, var6);
                    }

                    if (!var10.isEmpty()) {
                        var1.drop(var10, true);
                    }

                    this.synchronize(var1);
                } else {
                    this.restoreTransfer(var1, var7, var8, var9);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player var1, int var2) {
        if (var2 >= 0 && var2 < this.slots.size()) {
            Slot var3 = this.slots.get(var2);
            if (var3.hasItem() && var3.mayPickup(var1)) {
                ItemStack var4 = var3.getItem();
                ItemStack var5 = var4.copy();
                if (var2 < 27) {
                    if (!this.moveItemStackTo(var4, 27, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(var4, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }

                if (var4.isEmpty()) {
                    var3.set(ItemStack.EMPTY);
                } else {
                    var3.setChanged();
                }

                var3.onTake(var1, var4);
                return var5;
            } else {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean clickMenuButton(Player var1, int var2) {
        if (!(var1 instanceof ServerPlayer var3) || !this.stillValid(var1)) {
            return false;
        }

        if (var2 == 28) {
            var3.closeContainer();
            StatueMenuNetwork.openOrdinary(var3, this.statuePos);
            return true;
        }

        if (var2 == 27) {
            var3.closeContainer();
            StatueMenuNetwork.open(var3, this.statuePos);
            return true;
        } else {
            net.farmingtales.ftboss.util.SwordCavityRules.Slot var4 = net.farmingtales.ftboss.util.SwordCavityRules.Slot.byIndex(var2);
            if (var4 != null && this.getCarried().isEmpty() && this.reference(var2)) {
                ItemStack var5 = this.weapon();
                NativeStackComponents.Snapshot var6 = NativeStackComponents.capture(var5);
                int var7 = this.expectedRevision;

                try {
                    if (!WeaponCavityMenuRules.clearReference(var3, var5, var4, this.expectedRevision)) {
                        return false;
                    } else {
                        this.expectedRevision = WeaponCavityMenuRules.revision(var5);
                        var3.inventoryMenu.broadcastChanges();
                        this.broadcastChanges();
                        return true;
                    }
                } catch (RuntimeException var9) {
                    var6.restore(var5);
                    this.expectedRevision = var7;
                    transferFailure(var3, var9);
                    this.synchronize(var3);
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public void bindTransaction(Snapshot var1) {
        this.transactionSession = var1.session();
        this.transactionBinding = var1.binding();
    }

    public boolean transactionClick(ServerPlayer var1, Request var2) {
        if (var2.session().equals(this.transactionSession) && this.transactionMatches(var2.binding()) && this.stillValid(var1)) {
            if (var2.part() == -999 && var2.operation() != Operation.MENU_QUICK_CRAFT && !this.getCarried().isEmpty()) {
                return false;
            } else {
                ClickType var3 = switch (var2.operation()) {
                    case MENU_PICKUP -> ClickType.PICKUP;
                    case MENU_QUICK_MOVE -> ClickType.QUICK_MOVE;
                    case MENU_SWAP -> ClickType.SWAP;
                    case MENU_QUICK_CRAFT -> ClickType.QUICK_CRAFT;
                    case MENU_PICKUP_ALL -> ClickType.PICKUP_ALL;
                    default -> null;
                };
                if (var3 == null) {
                    return false;
                } else if (var3 == ClickType.SWAP && var2.part() < 27 && !this.swapFits(var2.part(), var2.value())) {
                    return false;
                } else {
                    Contents var4 = this.transferContents();
                    HashSet var5 = new HashSet();

                    for (int var6 = 0; var6 < 27; var6++) {
                        if (!WeaponCavityMenuRules.cellUnlocked(this.weapon(), var6)) {
                            var5.add(var6);
                        }
                    }

                    this.transferRejected = false;
                    this.authorizedClick = true;

                    boolean var10;
                    try {
                        this.clicked(var2.part(), var2.value(), var3, var1);
                        if (!TransferInvariant.valid(var4, this.transferContents(), this.weaponSlot, var5)) {
                            this.transferRejected = true;
                        }

                        var10 = !this.transferRejected;
                    } finally {
                        this.authorizedClick = false;
                    }

                    return var10;
                }
            }
        } else {
            return false;
        }
    }

    public List<ItemStack> transactionContents() {
        return this.contents();
    }

    public boolean transactionMatches(Binding var1) {
        return this.transactionSession != null && var1.equals(this.transactionBinding) && this.weapon() == this.boundSource;
    }

    public boolean transactionRemoveReference(ServerPlayer var1, Request var2) {
        if (var2.session().equals(this.transactionSession) && this.transactionMatches(var2.binding())) {
            this.authorizedClick = true;

            boolean var3;
            try {
                var3 = this.clickMenuButton(var1, var2.part());
            } finally {
                this.authorizedClick = false;
            }

            return var3;
        } else {
            return false;
        }
    }

    public void transactionRestore(List<ItemStack> var1) {
        for (int var2 = 0; var2 < var1.size(); var2++) {
            this.cavity.setItem(var2, ((ItemStack)var1.get(var2)).copy());
        }

        this.expectedRevision = WeaponCavityMenuRules.revision(this.weapon());
        this.expectedFingerprint = ForgeTransactionHost.fingerprint(this.weapon(), this.inventory.player.registryAccess());
    }

    private boolean swapFits(int var1, int var2) {
        if (var2 == this.weaponSlot) {
            return false;
        } else {
            ItemStack var3 = this.inventory.getItem(var2);
            ItemStack var4 = this.cavity.getItem(var1);
            if (var3.getCount() > 1 && !var4.isEmpty()) {
                int var5 = var4.getCount();

                for (int var6 = 0; var6 < 36; var6++) {
                    if (var6 != this.weaponSlot && var6 != var2) {
                        ItemStack var7 = this.inventory.getItem(var6);
                        if (var7.isEmpty()) {
                            return true;
                        }

                        if (ItemStack.isSameItemSameComponents(var7, var4)) {
                            var5 -= Math.max(0, var7.getMaxStackSize() - var7.getCount());
                        }
                    }
                }

                return var5 <= 0;
            } else {
                return true;
            }
        }
    }

    private Contents transferContents() {
        List var1 = this.contents().stream().map(this::transferStack).toList();
        ArrayList var2 = new ArrayList();

        for (int var3 = 0; var3 < this.inventory.getContainerSize(); var3++) {
            var2.add(transferStack(var3 == this.weaponSlot ? ItemStack.EMPTY : this.inventory.getItem(var3)));
        }

        return new Contents(var1, var2, transferStack(this.getCarried()), List.of());
    }

    private Stack transferStack(ItemStack var0) {
        if (var0.isEmpty()) {
            return new Stack("", 0);
        } else {
            ItemStack var1 = var0.copy();
            var1.setCount(1);
            return new Stack(ForgeTransactionHost.fingerprint(var1, this.inventory.player.registryAccess()), var0.getCount());
        }
    }

    private boolean replaceFromCursor(ServerPlayer var1, int var2) {
        ItemStack var3 = this.getCarried();
        ItemStack var4 = this.cavity.getItem(var2);
        if (var3.getCount() > 1 && !var4.isEmpty() && !ItemStack.isSameItemSameComponents(var3, var4)) {
            Slot var5 = this.slots.get(var2);
            if (var5.mayPlace(var3) && var5.mayPickup(var1)) {
                int var6 = -1;

                for (int var7 = 0; var7 < this.inventory.items.size(); var7++) {
                    if (var7 != this.weaponSlot) {
                        ItemStack var8 = this.inventory.getItem(var7);
                        if (var8.isEmpty()
                            || ItemStack.isSameItemSameComponents(var8, var4)
                                && var8.getCount() + var4.getCount() <= Math.min(var8.getMaxStackSize(), this.inventory.getMaxStackSize())) {
                            var6 = var7;
                            break;
                        }
                    }
                }

                if (var6 < 0) {
                    return true;
                } else {
                    List var13 = this.contents();
                    List var14 = this.inventoryContents();
                    ItemStack var9 = var3.copy();

                    try {
                        ItemStack var10 = var3.copy();
                        var10.setCount(1);
                        ItemStack var11 = this.inventory.getItem(var6);
                        if (var11.isEmpty()) {
                            this.inventory.setItem(var6, var4.copy());
                        } else {
                            var11.grow(var4.getCount());
                        }

                        this.cavity.setItem(var2, var10);
                        var3.shrink(1);
                        if (var3.isEmpty()) {
                            this.setCarried(ItemStack.EMPTY);
                        }

                        if (!this.commit(var1)) {
                            this.restoreTransfer(var1, var13, var14, var9);
                        }
                    } catch (RuntimeException var12) {
                        this.restoreTransfer(var1, var13, var14, var9);
                        transferFailure(var1, var12);
                    }

                    return true;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void clicked(int var1, int var2, ClickType var3, Player var4) {
        if (var3 != ClickType.SWAP || var2 != this.weaponSlot) {
            if (var1 < 27 || var1 >= this.slots.size() || this.slots.get(var1).getContainerSlot() != this.weaponSlot) {
                if (var4 instanceof ServerPlayer var5) {
                    if (!this.stillValid(var4)) {
                        var5.closeContainer();
                    } else if (var3 != ClickType.PICKUP
                        || var2 != 1
                        || var1 < 0
                        || var1 >= 27
                        || !FourWeaponFoundationAdapter.applies(this.weapon())
                        || !FourWeaponFoundationAdapter.matchesType(this.weapon(), "bow")
                            && !FourWeaponFoundationAdapter.matchesType(this.weapon(), "crossbow")
                        || !this.replaceFromCursor(var5, var1)) {
                        if (var3 == ClickType.SWAP && var1 >= 0 && var1 < 27) {
                            this.swapFromInventory(var5, var1, var2);
                        } else if (var3 == ClickType.THROW && var1 >= 0 && var1 < 27) {
                            Slot var14 = this.slots.get(var1);
                            if (this.getCarried().isEmpty() && var14.hasItem() && var14.mayPickup(var4)) {
                                ItemStack var15 = this.cavity.removeItem(var1, 1);
                                if (this.commit(var5)) {
                                    var14.onTake(var4, var15);
                                    var4.drop(var15, true);
                                } else {
                                    this.cavity.setItem(var1, var15);
                                }

                                this.synchronize(var5);
                            }
                        } else {
                            List var6 = this.contents();
                            List var7 = this.inventoryContents();
                            ItemStack var8 = this.getCarried().copy();

                            try {
                                super.clicked(var1, var2, var3, var4);
                            } catch (RuntimeException var13) {
                                this.restoreTransfer(var5, var6, var7, var8);
                                transferFailure(var5, var13);
                                return;
                            }

                            boolean var9 = false;

                            for (int var10 = 0; var10 < var6.size(); var10++) {
                                ItemStack var11 = (ItemStack)var6.get(var10);
                                ItemStack var12 = this.cavity.getItem(var10);
                                if (var11.getCount() != var12.getCount() || !ItemStack.isSameItemSameComponents(var11, var12)) {
                                    var9 = true;
                                }
                            }

                            if (var9 && !this.commit(var5)) {
                                this.restoreTransfer(var5, var6, var7, var8);
                            }
                        }
                    }
                } else {
                    super.clicked(var1, var2, var3, var4);
                }
            }
        }
    }

    private final class CavitySlot extends Slot {
        private CavitySlot(int var2) {
            super(SwordCavityMenu.this.cavity, var2, 17 + SwordCavityMenu.this.positions[var2] % 9 * 18, 44 + SwordCavityMenu.this.positions[var2] / 9 * 18);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack var1) {
            return WeaponCavityMenuRules.cellUnlocked(SwordCavityMenu.this.weapon(), this.getContainerSlot())
                && SwordCavityMenu.this.accepts(this.getContainerSlot(), var1);
        }

        @Override
        public boolean mayPickup(Player var1) {
            return var1 == SwordCavityMenu.this.inventory.player && SwordCavityMenu.this.accepts(this.getContainerSlot(), ItemStack.EMPTY);
        }
    }

    private final class InventorySlot extends Slot {
        private InventorySlot(int var2, int var3, int var4) {
            super(SwordCavityMenu.this.inventory, var2, var3, var4);
        }

        @Override
        public boolean mayPlace(ItemStack var1) {
            return this.getContainerSlot() != SwordCavityMenu.this.weaponSlot;
        }

        @Override
        public boolean mayPickup(Player var1) {
            return this.getContainerSlot() != SwordCavityMenu.this.weaponSlot && var1 == SwordCavityMenu.this.inventory.player;
        }
    }
}
