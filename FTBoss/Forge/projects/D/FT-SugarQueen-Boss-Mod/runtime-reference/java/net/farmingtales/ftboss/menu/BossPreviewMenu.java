/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.inventory.DataSlot
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.registries.ForgeRegistries
 */
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
import net.minecraft.world.Container;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class BossPreviewMenu
extends AbstractContainerMenu {
    private final SimpleContainer input = new SimpleContainer(1);
    private final Player owner;
    private final ContainerLevelAccess access;
    private final DataSlot profile = DataSlot.m_39401_();
    private final DataSlot killsLow = DataSlot.m_39401_();
    private final DataSlot killsHigh = DataSlot.m_39401_();

    public BossPreviewMenu(int n, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        this(n, inventory, friendlyByteBuf.m_130135_());
    }

    public BossPreviewMenu(int n, Inventory inventory, BlockPos blockPos) {
        super((MenuType)BlankStatueContent.MENU.get(), n);
        int n2;
        this.owner = inventory.f_35978_;
        this.access = ContainerLevelAccess.m_39289_((Level)this.owner.m_9236_(), (BlockPos)blockPos);
        this.profile.m_6422_(-1);
        this.m_38895_(this.profile);
        this.m_38895_(this.killsLow);
        this.m_38895_(this.killsHigh);
        this.m_38897_(new Slot((Container)this.input, 0, 24, 157){

            public boolean m_5857_(ItemStack itemStack) {
                return BossPreviewRules.accepts(BossPreviewMenu.itemId(itemStack));
            }

            public int m_6641_() {
                return 1;
            }
        });
        for (n2 = 0; n2 < 3; ++n2) {
            for (int i = 0; i < 9; ++i) {
                this.m_38897_(new Slot((Container)inventory, i + n2 * 9 + 9, 148 + i * 18, 145 + n2 * 18));
            }
        }
        for (n2 = 0; n2 < 9; ++n2) {
            this.m_38897_(new Slot((Container)inventory, n2, 148 + n2 * 18, 203));
        }
    }

    public int profile() {
        return this.profile.m_6501_();
    }

    public int playerKills() {
        return BossPreviewSummaryRules.showsKills(this.profile()) ? BossPreviewSummaryRules.kills(this.killsLow.m_6501_(), this.killsHigh.m_6501_()) : 0;
    }

    public static String itemId(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return "";
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    public void insertFromHand(InteractionHand interactionHand) {
        ItemStack itemStack = this.owner.m_21120_(interactionHand);
        if (this.input.m_7983_() && BossPreviewRules.accepts(BossPreviewMenu.itemId(itemStack))) {
            this.input.m_6836_(0, itemStack.m_41620_(1));
        }
    }

    public void m_38946_() {
        if (!this.owner.m_9236_().f_46443_) {
            String string = BossPreviewMenu.itemId(this.input.m_8020_(0));
            boolean bl = "tridents_n_stuff:sanguine_bond".equals(string) && BossPreviewTitleAccess.wearingManorTitle(this.owner);
            this.profile.m_6422_(BossPreviewRules.resolve(string, bl));
            int n = 0;
            Player player = this.owner;
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                if (BossPreviewSummaryRules.showsKills(this.profile()) && (player = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(BossPreviewSummaryRules.entityId(this.profile())))) != null) {
                    n = serverPlayer.m_8951_().m_13015_(Stats.f_12986_.m_12902_((Object)player));
                }
            }
            this.killsLow.m_6422_(BossPreviewSummaryRules.lowBits(n));
            this.killsHigh.m_6422_(BossPreviewSummaryRules.highBits(n));
        }
        super.m_38946_();
    }

    public ItemStack m_7648_(Player player, int n) {
        if (n < 0 || n >= this.f_38839_.size()) {
            return ItemStack.f_41583_;
        }
        Slot slot = (Slot)this.f_38839_.get(n);
        if (!slot.m_6657_()) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack = slot.m_7993_();
        ItemStack itemStack2 = itemStack.m_41777_();
        if (n == 0 ? !this.m_38903_(itemStack, 1, this.f_38839_.size(), true) : !((Slot)this.f_38839_.get(0)).m_5857_(itemStack) || !this.m_38903_(itemStack, 0, 1, false)) {
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

    public boolean m_6875_(Player player) {
        return player.m_6084_() && BossPreviewMenu.m_38889_((ContainerLevelAccess)this.access, (Player)player, (Block)((Block)BlankStatueContent.BLOCK.get()));
    }

    public void m_6877_(Player player) {
        super.m_6877_(player);
        if (!player.m_9236_().f_46443_) {
            this.m_150411_(player, (Container)this.input);
        }
    }

    public BlockPos statuePos() {
        return (BlockPos)this.access.m_39299_((level, blockPos) -> blockPos, (Object)BlockPos.f_121853_);
    }
}

