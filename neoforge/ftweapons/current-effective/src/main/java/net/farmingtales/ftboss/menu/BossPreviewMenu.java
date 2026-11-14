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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BossPreviewMenu extends AbstractContainerMenu {
    private final SimpleContainer input = new SimpleContainer(1);
    private final Player owner;
    private final ContainerLevelAccess access;
    private final DataSlot profile = DataSlot.standalone();
    private final DataSlot killsLow = DataSlot.standalone();
    private final DataSlot killsHigh = DataSlot.standalone();

    public BossPreviewMenu(int var1, Inventory var2, FriendlyByteBuf var3) {
        this(var1, var2, var3.readBlockPos());
    }

    public BossPreviewMenu(int var1, Inventory var2, BlockPos var3) {
        super((MenuType<?>)BlankStatueContent.MENU.get(), var1);
        this.owner = var2.player;
        this.access = ContainerLevelAccess.create(this.owner.level(), var3);
        this.profile.set(-1);
        this.addDataSlot(this.profile);
        this.addDataSlot(this.killsLow);
        this.addDataSlot(this.killsHigh);
        this.addSlot(new Slot(this.input, 0, 24, 157) {
            @Override
            public boolean mayPlace(ItemStack var1) {
                return BossPreviewRules.accepts(BossPreviewMenu.itemId(var1));
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int var4 = 0; var4 < 3; var4++) {
            for (int var5 = 0; var5 < 9; var5++) {
                this.addSlot(new Slot(var2, var5 + var4 * 9 + 9, 148 + var5 * 18, 145 + var4 * 18));
            }
        }

        for (int var6 = 0; var6 < 9; var6++) {
            this.addSlot(new Slot(var2, var6, 148 + var6 * 18, 203));
        }
    }

    public int profile() {
        return this.profile.get();
    }

    public int playerKills() {
        return BossPreviewSummaryRules.showsKills(this.profile()) ? BossPreviewSummaryRules.kills(this.killsLow.get(), this.killsHigh.get()) : 0;
    }

    public static String itemId(ItemStack var0) {
        if (var0.isEmpty()) {
            return "";
        } else {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            return var1 == null ? "" : var1.toString();
        }
    }

    public void insertFromHand(InteractionHand var1) {
        ItemStack var2 = this.owner.getItemInHand(var1);
        if (this.input.isEmpty() && BossPreviewRules.accepts(itemId(var2))) {
            this.input.setItem(0, var2.split(1));
        }
    }

    @Override
    public void broadcastChanges() {
        if (!this.owner.level().isClientSide) {
            String var1 = itemId(this.input.getItem(0));
            boolean var2 = "tridents_n_stuff:sanguine_bond".equals(var1) && BossPreviewTitleAccess.wearingManorTitle(this.owner);
            this.profile.set(BossPreviewRules.resolve(var1, var2));
            int var3 = 0;
            if (this.owner instanceof ServerPlayer var4 && BossPreviewSummaryRules.showsKills(this.profile())) {
                EntityType var6 = (EntityType)BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(BossPreviewSummaryRules.entityId(this.profile()))).orElse(null);
                if (var6 != null) {
                    var3 = var4.getStats().getValue(Stats.ENTITY_KILLED.get(var6));
                }
            }

            this.killsLow.set(BossPreviewSummaryRules.lowBits(var3));
            this.killsHigh.set(BossPreviewSummaryRules.highBits(var3));
        }

        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player var1, int var2) {
        if (var2 >= 0 && var2 < this.slots.size()) {
            Slot var3 = this.slots.get(var2);
            if (!var3.hasItem()) {
                return ItemStack.EMPTY;
            } else {
                ItemStack var4 = var3.getItem();
                ItemStack var5 = var4.copy();
                if (var2 == 0) {
                    if (!this.moveItemStackTo(var4, 1, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.slots.get(0).mayPlace(var4) || !this.moveItemStackTo(var4, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }

                if (var4.isEmpty()) {
                    var3.set(ItemStack.EMPTY);
                } else {
                    var3.setChanged();
                }

                var3.onTake(var1, var4);
                return var5;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean stillValid(Player var1) {
        return var1.isAlive() && stillValid(this.access, var1, (Block)BlankStatueContent.BLOCK.get());
    }

    @Override
    public void removed(Player var1) {
        super.removed(var1);
        if (!var1.level().isClientSide) {
            this.clearContainer(var1, this.input);
        }
    }

    public BlockPos statuePos() {
        return this.access.evaluate((var0, var1) -> var1, BlockPos.ZERO);
    }
}
