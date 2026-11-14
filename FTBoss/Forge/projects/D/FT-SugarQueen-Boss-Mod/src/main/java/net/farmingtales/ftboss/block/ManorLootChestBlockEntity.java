package net.farmingtales.ftboss.block;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ManorLootChestBlockEntity extends RandomizableContainerBlockEntity {
    public static final int CAPACITY = 54;
    private NonNullList<ItemStack> items = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    private UUID victory;

    public ManorLootChestBlockEntity(BlockPos position, BlockState state) { super(FTBlocks.MANOR_LOOT_ENTITY.get(), position, state); }

    public void setVictory(UUID identifier) { victory = identifier; setChanged(); }
    public boolean belongsTo(UUID identifier) { return identifier.equals(victory); }

    @Override
    public int getContainerSize() { return CAPACITY; }

    @Override
    protected Component getDefaultName() { return Component.translatable("block.ftboss.manor_loot_chest"); }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) { return ChestMenu.sixRows(containerId, inventory, this); }

    @Override
    protected NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void setItems(NonNullList<ItemStack> contents) { items = contents; }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items);
        victory = tag.hasUUID("ManorVictory") ? tag.getUUID("ManorVictory") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items);
        if (victory != null) tag.putUUID("ManorVictory", victory);
    }
}
