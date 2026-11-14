/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.slf4j.Logger
 */
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
import net.farmingtales.ftboss.menu.WeaponCavityMenuRules;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransferInvariant;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
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

public final class SwordCavityMenu
extends AbstractContainerMenu {
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
    private FourWeaponTransactions.Binding transactionBinding;
    private boolean transferRejected;
    private boolean authorizedClick;
    private final ItemStack boundSource;
    private String expectedFingerprint;
    private final ResourceKey<Level> boundDimension;

    public SwordCavityMenu(int n, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        this(n, inventory, friendlyByteBuf.m_130135_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), SwordCavityMenu.readLayout(friendlyByteBuf));
    }

    public SwordCavityMenu(int n, Inventory inventory, BlockPos blockPos, int n2, UUID uUID) {
        this(n, inventory, blockPos, n2, uUID, SwordCavityExtensions.layout(inventory.m_8020_(n2)));
    }

    public SwordCavityMenu(int n, Inventory inventory, BlockPos blockPos, int n2, UUID uUID, int[] nArray) {
        super((MenuType)BlankStatueContent.SWORD_CAVITY_MENU.get(), n);
        int n3;
        this.inventory = inventory;
        this.statuePos = blockPos.m_7949_();
        this.weaponSlot = n2;
        this.weaponId = uUID;
        this.positions = WeaponCavityMenuRules.layout(nArray, this.weapon());
        this.expectedRevision = WeaponCavityMenuRules.revision(this.weapon());
        List<ItemStack> list = WeaponCavityMenuRules.containerItems(this.weapon());
        for (n3 = 0; n3 < 27; ++n3) {
            this.cavity.m_6836_(n3, list.get(n3).m_41777_());
            this.m_38897_(new CavitySlot(n3));
        }
        for (n3 = 0; n3 < 3; ++n3) {
            for (int i = 0; i < 9; ++i) {
                this.m_38897_(new InventorySlot(i + n3 * 9 + 9, 17 + i * 18, 124 + n3 * 18));
            }
        }
        for (n3 = 0; n3 < 9; ++n3) {
            this.m_38897_(new InventorySlot(n3, 17 + n3 * 18, 182));
        }
        this.boundSource = this.weapon();
        this.expectedFingerprint = ForgeTransactionHost.fingerprint(this.boundSource);
        this.boundDimension = this.inventory.f_35978_.m_9236_().m_46472_();
    }

    private static int[] readLayout(FriendlyByteBuf friendlyByteBuf) {
        int[] nArray = new int[27];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = friendlyByteBuf.m_130242_();
        }
        return SwordCavityExtensions.validateLayout(nArray);
    }

    public ItemStack weapon() {
        return this.weaponSlot < 0 || this.weaponSlot >= this.inventory.m_6643_() ? ItemStack.f_41583_ : this.inventory.m_8020_(this.weaponSlot);
    }

    public boolean matches(BlockPos blockPos) {
        return this.statuePos.equals((Object)blockPos);
    }

    public boolean reference(int n) {
        if (n >= 0 && n < 27 && this.cavity.m_8020_(n).m_41619_()) {
            return WeaponCavityMenuRules.reference(this.weapon(), n);
        }
        return false;
    }

    private List<ItemStack> contents() {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (int i = 0; i < 27; ++i) {
            arrayList.add(this.cavity.m_8020_(i).m_41777_());
        }
        return arrayList;
    }

    private boolean accepts(int n, ItemStack itemStack) {
        if (!WeaponCavityMenuRules.containerEditable(this.weapon())) {
            return false;
        }
        List<ItemStack> list = this.contents();
        ItemStack itemStack2 = itemStack.m_41777_();
        if (!itemStack2.m_41619_()) {
            itemStack2.m_41764_(1);
        }
        list.set(n, itemStack2);
        return WeaponCavityMenuRules.canSetContainerItems(this.weapon(), list);
    }

    private boolean commit(ServerPlayer serverPlayer) {
        ItemStack itemStack = this.weapon();
        CompoundTag compoundTag = itemStack.m_41783_() == null ? null : itemStack.m_41783_().m_6426_();
        int n = this.expectedRevision;
        try {
            if (!WeaponCavityMenuRules.setContainerItems(serverPlayer, itemStack, this.contents(), this.expectedRevision)) {
                return false;
            }
            this.expectedRevision = WeaponCavityMenuRules.revision(itemStack);
            serverPlayer.f_36095_.m_38946_();
            return true;
        }
        catch (RuntimeException runtimeException) {
            itemStack.m_41751_(compoundTag);
            this.expectedRevision = n;
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
            return false;
        }
    }

    private static void transferFailure(ServerPlayer serverPlayer, RuntimeException runtimeException) {
        try {
            LOGGER.error("[FTBoss][SwordCavity] Material transfer rolled back", (Throwable)runtimeException);
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.sword.transfer_failed"), true);
        }
        catch (RuntimeException runtimeException2) {
            // empty catch block
        }
    }

    private List<ItemStack> inventoryContents() {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (int i = 0; i < this.inventory.m_6643_(); ++i) {
            arrayList.add(this.inventory.m_8020_(i).m_41777_());
        }
        return arrayList;
    }

    private void synchronize(ServerPlayer serverPlayer) {
        try {
            serverPlayer.f_36095_.m_38946_();
        }
        catch (RuntimeException runtimeException) {
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
        }
        try {
            this.m_182423_();
        }
        catch (RuntimeException runtimeException) {
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
        }
    }

    private void restoreTransfer(ServerPlayer serverPlayer, List<ItemStack> list, List<ItemStack> list2, ItemStack itemStack) {
        int n;
        for (n = 0; n < list.size(); ++n) {
            this.cavity.m_6836_(n, list.get(n));
        }
        for (n = 0; n < list2.size(); ++n) {
            if (n == this.weaponSlot) continue;
            this.inventory.m_6836_(n, list2.get(n));
        }
        this.m_142503_(itemStack);
        this.synchronize(serverPlayer);
    }

    public boolean m_6875_(Player player) {
        if (player != this.inventory.f_35978_ || !player.m_6084_() || player.m_5833_() || player.m_5803_() || player.m_6117_()) {
            return false;
        }
        if (player.m_9236_().f_46443_) {
            return true;
        }
        ItemStack itemStack = this.weapon();
        return WeaponCavityMenuRules.applies(itemStack) && itemStack.m_41613_() == 1 && this.weaponId.equals(FoodWeaponLifecycle.weaponId(itemStack)) && this.expectedRevision == WeaponCavityMenuRules.revision(itemStack) && player.m_9236_().m_46805_(this.statuePos) && player.m_9236_().m_8055_(this.statuePos).m_60734_() instanceof CandyStatueBlock && player.m_20275_((double)this.statuePos.m_123341_() + 0.5, (double)this.statuePos.m_123342_() + 1.0, (double)this.statuePos.m_123343_() + 0.5) <= 64.0;
    }

    private void swapFromInventory(ServerPlayer serverPlayer, int n, int n2) {
        if (n2 < 0 || n2 > 8 && n2 != 40 || n2 >= this.inventory.m_6643_() || n2 == this.weaponSlot) {
            return;
        }
        Slot slot = (Slot)this.f_38839_.get(n);
        ItemStack itemStack = this.inventory.m_8020_(n2);
        ItemStack itemStack2 = this.cavity.m_8020_(n).m_41777_();
        if (itemStack.m_41619_() && itemStack2.m_41619_() || !itemStack2.m_41619_() && !slot.m_8010_((Player)serverPlayer) || !itemStack.m_41619_() && !slot.m_5857_(itemStack)) {
            return;
        }
        List<ItemStack> list = this.contents();
        List<ItemStack> list2 = this.inventoryContents();
        ItemStack itemStack3 = this.m_142621_().m_41777_();
        ItemStack itemStack4 = ItemStack.f_41583_;
        try {
            ItemStack itemStack5 = itemStack.m_41777_();
            if (!itemStack5.m_41619_()) {
                itemStack5.m_41764_(1);
            }
            this.cavity.m_6836_(n, itemStack5);
            if (itemStack.m_41619_() || itemStack.m_41613_() == 1) {
                this.inventory.m_6836_(n2, itemStack2.m_41777_());
            } else {
                itemStack.m_41774_(1);
                itemStack4 = itemStack2.m_41777_();
                if (!itemStack4.m_41619_()) {
                    this.inventory.m_36054_(itemStack4);
                }
            }
        }
        catch (RuntimeException runtimeException) {
            this.restoreTransfer(serverPlayer, list, list2, itemStack3);
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
            return;
        }
        if (this.commit(serverPlayer)) {
            if (!itemStack2.m_41619_()) {
                slot.m_142406_((Player)serverPlayer, itemStack2);
            }
            if (!itemStack4.m_41619_()) {
                serverPlayer.m_36176_(itemStack4, true);
            }
        } else {
            this.restoreTransfer(serverPlayer, list, list2, itemStack3);
            return;
        }
        this.synchronize(serverPlayer);
    }

    public ItemStack m_7648_(Player player, int n) {
        if (n < 0 || n >= this.f_38839_.size()) {
            return ItemStack.f_41583_;
        }
        Slot slot = (Slot)this.f_38839_.get(n);
        if (!slot.m_6657_() || !slot.m_8010_(player)) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack = slot.m_7993_();
        ItemStack itemStack2 = itemStack.m_41777_();
        if (n < 27 ? !this.m_38903_(itemStack, 27, this.f_38839_.size(), true) : !this.m_38903_(itemStack, 0, 27, false)) {
            return ItemStack.f_41583_;
        }
        if (itemStack.m_41619_()) {
            slot.m_5852_(ItemStack.f_41583_);
        } else {
            slot.m_6654_();
        }
        slot.m_142406_(player, itemStack);
        return itemStack2;
    }

    public boolean m_6366_(Player player, int n) {
        ServerPlayer serverPlayer;
        block9: {
            block8: {
                if (!(player instanceof ServerPlayer)) break block8;
                serverPlayer = (ServerPlayer)player;
                if (this.m_6875_(player)) break block9;
            }
            return false;
        }
        if (n == 27) {
            serverPlayer.m_6915_();
            StatueMenuNetwork.open(serverPlayer, this.statuePos);
            return true;
        }
        SwordCavityRules.Slot slot = SwordCavityRules.Slot.byIndex(n);
        if (slot == null || !this.m_142621_().m_41619_() || !this.reference(n)) {
            return false;
        }
        ItemStack itemStack = this.weapon();
        CompoundTag compoundTag = itemStack.m_41783_() == null ? null : itemStack.m_41783_().m_6426_();
        int n2 = this.expectedRevision;
        try {
            if (!WeaponCavityMenuRules.clearReference(serverPlayer, itemStack, slot, this.expectedRevision)) {
                return false;
            }
            this.expectedRevision = WeaponCavityMenuRules.revision(itemStack);
            serverPlayer.f_36095_.m_38946_();
            this.m_38946_();
            return true;
        }
        catch (RuntimeException runtimeException) {
            itemStack.m_41751_(compoundTag);
            this.expectedRevision = n2;
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
            this.synchronize(serverPlayer);
            return false;
        }
    }

    public void bindTransaction(FourWeaponTransactions.Snapshot snapshot) {
        this.transactionSession = snapshot.session();
        this.transactionBinding = snapshot.binding();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean transactionClick(ServerPlayer serverPlayer, FourWeaponTransactions.Request request) {
        boolean bl;
        ClickType clickType;
        if (!(request.session().equals(this.transactionSession) && this.transactionMatches(request.binding()) && this.m_6875_((Player)serverPlayer))) {
            return false;
        }
        if (request.part() == -999 && request.operation() != FourWeaponTransactions.Operation.MENU_QUICK_CRAFT && !this.m_142621_().m_41619_()) {
            return false;
        }
        switch (request.operation()) {
            case MENU_PICKUP: {
                ClickType clickType2 = ClickType.PICKUP;
                break;
            }
            case MENU_QUICK_MOVE: {
                ClickType clickType2 = ClickType.QUICK_MOVE;
                break;
            }
            case MENU_SWAP: {
                ClickType clickType2 = ClickType.SWAP;
                break;
            }
            case MENU_QUICK_CRAFT: {
                ClickType clickType2 = ClickType.QUICK_CRAFT;
                break;
            }
            case MENU_PICKUP_ALL: {
                ClickType clickType2 = ClickType.PICKUP_ALL;
                break;
            }
            default: {
                ClickType clickType2 = clickType = null;
            }
        }
        if (clickType == null) {
            return false;
        }
        if (clickType == ClickType.SWAP && request.part() < 27 && !this.swapFits(request.part(), request.value())) {
            return false;
        }
        TransferInvariant.Contents contents = this.transferContents();
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (bl = false; bl < 27 != 0; bl += 1) {
            if (WeaponCavityMenuRules.cellUnlocked(this.weapon(), bl ? 1 : 0)) continue;
            hashSet.add(bl ? 1 : 0);
        }
        this.transferRejected = false;
        this.authorizedClick = true;
        try {
            this.m_150399_(request.part(), request.value(), clickType, (Player)serverPlayer);
            if (!TransferInvariant.valid(contents, this.transferContents(), this.weaponSlot, hashSet)) {
                this.transferRejected = true;
            }
            bl = !this.transferRejected;
            return bl;
        }
        finally {
            this.authorizedClick = false;
        }
    }

    public List<ItemStack> transactionContents() {
        return this.contents();
    }

    public boolean transactionMatches(FourWeaponTransactions.Binding binding) {
        return this.transactionSession != null && binding.equals(this.transactionBinding) && this.weapon() == this.boundSource;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean transactionRemoveReference(ServerPlayer serverPlayer, FourWeaponTransactions.Request request) {
        if (!request.session().equals(this.transactionSession) || !this.transactionMatches(request.binding())) {
            return false;
        }
        this.authorizedClick = true;
        try {
            boolean bl = this.m_6366_((Player)serverPlayer, request.part());
            return bl;
        }
        finally {
            this.authorizedClick = false;
        }
    }

    public void transactionRestore(List<ItemStack> list) {
        for (int i = 0; i < list.size(); ++i) {
            this.cavity.m_6836_(i, list.get(i).m_41777_());
        }
        this.expectedRevision = WeaponCavityMenuRules.revision(this.weapon());
        this.expectedFingerprint = ForgeTransactionHost.fingerprint(this.weapon());
    }

    private boolean swapFits(int n, int n2) {
        if (n2 == this.weaponSlot) {
            return false;
        }
        ItemStack itemStack = this.inventory.m_8020_(n2);
        ItemStack itemStack2 = this.cavity.m_8020_(n);
        if (itemStack.m_41613_() <= 1 || itemStack2.m_41619_()) {
            return true;
        }
        int n3 = itemStack2.m_41613_();
        for (int i = 0; i < 36; ++i) {
            if (i == this.weaponSlot || i == n2) continue;
            ItemStack itemStack3 = this.inventory.m_8020_(i);
            if (itemStack3.m_41619_()) {
                return true;
            }
            if (!ItemStack.m_150942_((ItemStack)itemStack3, (ItemStack)itemStack2)) continue;
            n3 -= Math.max(0, itemStack3.m_41741_() - itemStack3.m_41613_());
        }
        return n3 <= 0;
    }

    private TransferInvariant.Contents transferContents() {
        List<TransferInvariant.Stack> list = this.contents().stream().map(SwordCavityMenu::transferStack).toList();
        ArrayList<TransferInvariant.Stack> arrayList = new ArrayList<TransferInvariant.Stack>();
        for (int i = 0; i < this.inventory.m_6643_(); ++i) {
            arrayList.add(SwordCavityMenu.transferStack(i == this.weaponSlot ? ItemStack.f_41583_ : this.inventory.m_8020_(i)));
        }
        return new TransferInvariant.Contents(list, arrayList, SwordCavityMenu.transferStack(this.m_142621_()), List.of());
    }

    private static TransferInvariant.Stack transferStack(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return new TransferInvariant.Stack("", 0);
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        itemStack2.m_41764_(1);
        return new TransferInvariant.Stack(ForgeTransactionHost.fingerprint(itemStack2), itemStack.m_41613_());
    }

    private boolean replaceFromCursor(ServerPlayer serverPlayer, int n) {
        ItemStack itemStack;
        ItemStack itemStack2 = this.m_142621_();
        ItemStack itemStack3 = this.cavity.m_8020_(n);
        if (itemStack2.m_41613_() <= 1 || itemStack3.m_41619_() || ItemStack.m_150942_((ItemStack)itemStack2, (ItemStack)itemStack3)) {
            return false;
        }
        Slot slot = (Slot)this.f_38839_.get(n);
        if (!slot.m_5857_(itemStack2) || !slot.m_8010_((Player)serverPlayer)) {
            return true;
        }
        int n2 = -1;
        for (int i = 0; i < this.inventory.f_35974_.size(); ++i) {
            if (i == this.weaponSlot || !(itemStack = this.inventory.m_8020_(i)).m_41619_() && (!ItemStack.m_150942_((ItemStack)itemStack, (ItemStack)itemStack3) || itemStack.m_41613_() + itemStack3.m_41613_() > Math.min(itemStack.m_41741_(), this.inventory.m_6893_()))) continue;
            n2 = i;
            break;
        }
        if (n2 < 0) {
            return true;
        }
        List<ItemStack> list = this.contents();
        itemStack = this.inventoryContents();
        ItemStack itemStack4 = itemStack2.m_41777_();
        try {
            ItemStack itemStack5 = itemStack2.m_41777_();
            itemStack5.m_41764_(1);
            ItemStack itemStack6 = this.inventory.m_8020_(n2);
            if (itemStack6.m_41619_()) {
                this.inventory.m_6836_(n2, itemStack3.m_41777_());
            } else {
                itemStack6.m_41769_(itemStack3.m_41613_());
            }
            this.cavity.m_6836_(n, itemStack5);
            itemStack2.m_41774_(1);
            if (itemStack2.m_41619_()) {
                this.m_142503_(ItemStack.f_41583_);
            }
            if (!this.commit(serverPlayer)) {
                this.restoreTransfer(serverPlayer, list, (List<ItemStack>)itemStack, itemStack4);
            }
        }
        catch (RuntimeException runtimeException) {
            this.restoreTransfer(serverPlayer, list, (List<ItemStack>)itemStack, itemStack4);
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
        }
        return true;
    }

    public void m_150399_(int n, int n2, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && n2 == this.weaponSlot) {
            return;
        }
        if (n >= 27 && n < this.f_38839_.size() && ((Slot)this.f_38839_.get(n)).m_150661_() == this.weaponSlot) {
            return;
        }
        if (!(player instanceof ServerPlayer)) {
            super.m_150399_(n, n2, clickType, player);
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (!this.m_6875_(player)) {
            serverPlayer.m_6915_();
            return;
        }
        if (clickType == ClickType.PICKUP && n2 == 1 && n >= 0 && n < 27 && FourWeaponFoundationAdapter.applies(this.weapon()) && (FourWeaponFoundationAdapter.matchesType(this.weapon(), "bow") || FourWeaponFoundationAdapter.matchesType(this.weapon(), "crossbow")) && this.replaceFromCursor(serverPlayer, n)) {
            return;
        }
        if (clickType == ClickType.SWAP && n >= 0 && n < 27) {
            this.swapFromInventory(serverPlayer, n, n2);
            return;
        }
        if (clickType == ClickType.THROW && n >= 0 && n < 27) {
            Slot slot = (Slot)this.f_38839_.get(n);
            if (!(this.m_142621_().m_41619_() && slot.m_6657_() && slot.m_8010_(player))) {
                return;
            }
            ItemStack itemStack = this.cavity.m_7407_(n, 1);
            if (this.commit(serverPlayer)) {
                slot.m_142406_(player, itemStack);
                player.m_36176_(itemStack, true);
            } else {
                this.cavity.m_6836_(n, itemStack);
            }
            this.synchronize(serverPlayer);
            return;
        }
        List<ItemStack> list = this.contents();
        List<ItemStack> list2 = this.inventoryContents();
        ItemStack itemStack = this.m_142621_().m_41777_();
        try {
            super.m_150399_(n, n2, clickType, player);
        }
        catch (RuntimeException runtimeException) {
            this.restoreTransfer(serverPlayer, list, list2, itemStack);
            SwordCavityMenu.transferFailure(serverPlayer, runtimeException);
            return;
        }
        boolean bl = false;
        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemStack2 = list.get(i);
            ItemStack itemStack3 = this.cavity.m_8020_(i);
            if (itemStack2.m_41613_() == itemStack3.m_41613_() && ItemStack.m_150942_((ItemStack)itemStack2, (ItemStack)itemStack3)) continue;
            bl = true;
        }
        if (bl && !this.commit(serverPlayer)) {
            this.restoreTransfer(serverPlayer, list, list2, itemStack);
        }
    }

    private final class CavitySlot
    extends Slot {
        private CavitySlot(int n) {
            super((Container)SwordCavityMenu.this.cavity, n, 17 + SwordCavityMenu.this.positions[n] % 9 * 18, 44 + SwordCavityMenu.this.positions[n] / 9 * 18);
        }

        public int m_6641_() {
            return 1;
        }

        public boolean m_5857_(ItemStack itemStack) {
            return WeaponCavityMenuRules.cellUnlocked(SwordCavityMenu.this.weapon(), this.m_150661_()) && SwordCavityMenu.this.accepts(this.m_150661_(), itemStack);
        }

        public boolean m_8010_(Player player) {
            return player == SwordCavityMenu.this.inventory.f_35978_ && SwordCavityMenu.this.accepts(this.m_150661_(), ItemStack.f_41583_);
        }
    }

    private final class InventorySlot
    extends Slot {
        private InventorySlot(int n, int n2, int n3) {
            super((Container)SwordCavityMenu.this.inventory, n, n2, n3);
        }

        public boolean m_5857_(ItemStack itemStack) {
            return this.m_150661_() != SwordCavityMenu.this.weaponSlot;
        }

        public boolean m_8010_(Player player) {
            return this.m_150661_() != SwordCavityMenu.this.weaponSlot && player == SwordCavityMenu.this.inventory.f_35978_;
        }
    }
}

