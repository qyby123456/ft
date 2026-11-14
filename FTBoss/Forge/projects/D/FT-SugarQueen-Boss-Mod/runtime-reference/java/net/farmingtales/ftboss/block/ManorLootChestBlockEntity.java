/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package net.farmingtales.ftboss.block;

import java.util.UUID;
import net.farmingtales.ftboss.block.FTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ManorLootChestBlockEntity
extends RandomizableContainerBlockEntity {
    public static final int CAPACITY = 54;
    private NonNullList<ItemStack> items = NonNullList.m_122780_((int)54, (Object)ItemStack.f_41583_);
    private UUID victory;

    public ManorLootChestBlockEntity(BlockPos position, BlockState state) {
        super((BlockEntityType)FTBlocks.MANOR_LOOT_ENTITY.get(), position, state);
    }

    public void setVictory(UUID identifier) {
        this.victory = identifier;
        this.m_6596_();
    }

    public boolean belongsTo(UUID identifier) {
        return identifier.equals(this.victory);
    }

    public int m_6643_() {
        return 54;
    }

    protected Component m_6820_() {
        return Component.m_237115_((String)"block.ftboss.manor_loot_chest");
    }

    protected AbstractContainerMenu m_6555_(int containerId, Inventory inventory) {
        return ChestMenu.m_39246_((int)containerId, (Inventory)inventory, (Container)this);
    }

    protected NonNullList<ItemStack> m_7086_() {
        return this.items;
    }

    protected void m_6520_(NonNullList<ItemStack> contents) {
        this.items = contents;
    }

    public void m_142466_(CompoundTag tag) {
        super.m_142466_(tag);
        this.items = NonNullList.m_122780_((int)54, (Object)ItemStack.f_41583_);
        if (!this.m_59631_(tag)) {
            ContainerHelper.m_18980_((CompoundTag)tag, this.items);
        }
        this.victory = tag.m_128403_("ManorVictory") ? tag.m_128342_("ManorVictory") : null;
    }

    protected void m_183515_(CompoundTag tag) {
        super.m_183515_(tag);
        if (!this.m_59634_(tag)) {
            ContainerHelper.m_18973_((CompoundTag)tag, this.items);
        }
        if (this.victory != null) {
            tag.m_128362_("ManorVictory", this.victory);
        }
    }
}

