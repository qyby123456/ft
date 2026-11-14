package net.farmingtales.ftboss.menu;

import net.farmingtales.ftboss.block.BlankStatueContent;
import net.farmingtales.ftboss.integ.BossPreviewTitleAccess;
import net.farmingtales.ftboss.util.BossPreviewRules;
import net.farmingtales.ftboss.util.BossPreviewSummaryRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 玩家独占临时槽；2026-10-12：服务端准入同步，关闭、死亡、断线均返还物品。 */
public final class BossPreviewMenu extends AbstractContainerMenu {
    private final SimpleContainer input = new SimpleContainer(1);
    private final Player owner;
    private final ContainerLevelAccess access;
    private final DataSlot profile = DataSlot.standalone();
    private final DataSlot killsLow = DataSlot.standalone();
    private final DataSlot killsHigh = DataSlot.standalone();

    public BossPreviewMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos());
    }

    public BossPreviewMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(BlankStatueContent.MENU.get(), containerId);
        owner = inventory.player;
        access = ContainerLevelAccess.create(owner.level(), pos);
        profile.set(BossPreviewRules.EMPTY);
        addDataSlot(profile);
        addDataSlot(killsLow);
        addDataSlot(killsHigh);
        addSlot(new Slot(input, 0, 24, 157) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BossPreviewRules.accepts(itemId(stack));
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 148 + column * 18, 145 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 148 + column * 18, 203));
        }
    }

    public int profile() {
        return profile.get();
    }

    public int playerKills() {
        return BossPreviewSummaryRules.showsKills(profile())
                ? BossPreviewSummaryRules.kills(killsLow.get(), killsHigh.get()) : 0;
    }

    public static String itemId(ItemStack stack) {
        if (stack.isEmpty()) return "";
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    public void insertFromHand(InteractionHand hand) {
        ItemStack held = owner.getItemInHand(hand);
        if (input.isEmpty() && BossPreviewRules.accepts(itemId(held))) input.setItem(0, held.split(1));
    }

    @Override
    public void broadcastChanges() {
        if (!owner.level().isClientSide) {
            String identifier = itemId(input.getItem(0));
            boolean wearingTitle = BossPreviewRules.CONTRACT.equals(identifier) && BossPreviewTitleAccess.wearingManorTitle(owner);
            profile.set(BossPreviewRules.resolve(identifier, wearingTitle));
            int kills = 0;
            if (owner instanceof ServerPlayer player && BossPreviewSummaryRules.showsKills(profile())) {
                var type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(BossPreviewSummaryRules.entityId(profile())));
                if (type != null) kills = player.getStats().getValue(Stats.ENTITY_KILLED.get(type));
            }
            killsLow.set(BossPreviewSummaryRules.lowBits(kills));
            killsHigh.set(BossPreviewSummaryRules.highBits(kills));
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack result = original.copy();
        if (slotIndex == 0) {
            if (!moveItemStackTo(original, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!slots.get(0).mayPlace(original) || !moveItemStackTo(original, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, original);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && stillValid(access, player, BlankStatueContent.BLOCK.get());
    }

    public BlockPos statuePos() {
        return access.evaluate((level, position) -> position, BlockPos.ZERO);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, input);
    }
}
