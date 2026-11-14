/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TridentItem
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.transaction;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeTransactionHost
implements FourWeaponTransactions.Host {
    private final Function<UUID, ServerPlayer> players;
    private final Boundary boundary;
    private final Map<UUID, Long> lastRequest = new HashMap<UUID, Long>();

    public ForgeTransactionHost(Function<UUID, ServerPlayer> function, Boundary boundary) {
        this.players = Objects.requireNonNull(function);
        this.boundary = Objects.requireNonNull(boundary);
    }

    @Override
    public FourWeaponTransactions.State read(UUID uUID, FourWeaponTransactions.Binding binding) {
        Object object;
        Object object2;
        SwordCavityMenu swordCavityMenu;
        AbstractContainerMenu abstractContainerMenu;
        ServerPlayer serverPlayer = this.players.apply(uUID);
        if (serverPlayer == null || serverPlayer.m_213877_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || serverPlayer.f_36096_ != serverPlayer.f_36095_ && (!((abstractContainerMenu = serverPlayer.f_36096_) instanceof SwordCavityMenu) || !(swordCavityMenu = (SwordCavityMenu)abstractContainerMenu).transactionMatches(binding)) || !serverPlayer.m_20148_().equals(uUID) || !binding.owner().equals(uUID) || !serverPlayer.m_9236_().m_46472_().m_135782_().toString().equals(binding.dimension())) {
            return null;
        }
        if (binding.hand() == 0 && serverPlayer.m_150109_().f_35977_ != binding.inventorySlot()) {
            return null;
        }
        swordCavityMenu = serverPlayer.m_21120_(binding.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (swordCavityMenu.m_41619_() || swordCavityMenu.m_41613_() != 1 || !this.verified((ItemStack)swordCavityMenu)) {
            return null;
        }
        abstractContainerMenu = ForgeRegistries.ITEMS.getKey((Object)swordCavityMenu.m_41720_());
        CompoundTag compoundTag = swordCavityMenu.m_41737_("FTBossWeaponLifecycle");
        if (!(abstractContainerMenu != null && abstractContainerMenu.toString().equals(binding.item()) && compoundTag != null && compoundTag.m_128403_("Weapon") && binding.weapon().equals(compoundTag.m_128342_("Weapon")))) {
            return null;
        }
        for (int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
            object2 = serverPlayer.m_150109_().m_8020_(i);
            object = object2.m_41737_("FTBossWeaponLifecycle");
            if (object2 == swordCavityMenu || object == null || !object.m_128403_("Weapon") || !binding.weapon().equals(object.m_128342_("Weapon"))) continue;
            return null;
        }
        BlockPos blockPos = BlockPos.m_122022_((long)binding.statue());
        if (!serverPlayer.m_9236_().m_46805_(blockPos) || !serverPlayer.m_9236_().m_6857_().m_61937_(blockPos) || serverPlayer.m_20275_((double)blockPos.m_123341_() + 0.5, (double)blockPos.m_123342_() + 1.0, (double)blockPos.m_123343_() + 0.5) > 64.0) {
            return null;
        }
        object2 = this.boundary.statueIdentity(serverPlayer, blockPos);
        if (object2 == null || !((String)object2).equals(binding.statueIdentity())) {
            return null;
        }
        object = this.boundary.policy(serverPlayer, (ItemStack)swordCavityMenu, blockPos);
        if (object == null) {
            return null;
        }
        return new FourWeaponTransactions.State(binding, serverPlayer, (Object)swordCavityMenu, ((Policy)object).cavityRevision(), ((Policy)object).repairRevision(), ((Policy)object).evolutionLevel(), ForgeTransactionHost.fingerprint((ItemStack)swordCavityMenu), ((Policy)object).rulesFingerprint(), ((Policy)object).allowed());
    }

    private boolean verified(ItemStack itemStack) {
        return itemStack.m_41720_() instanceof BowItem || itemStack.m_41720_() instanceof CrossbowItem || itemStack.m_41720_() instanceof AxeItem || itemStack.m_41720_() instanceof TridentItem || this.boundary.verifiedCustomItem(itemStack);
    }

    @Override
    public FourWeaponTransactions.Mutation prepare(UUID uUID, FourWeaponTransactions.Request request, FourWeaponTransactions.State state) {
        List<ItemStack> list;
        SwordCavityMenu swordCavityMenu;
        Object object;
        final ServerPlayer serverPlayer = this.players.apply(uUID);
        FourWeaponTransactions.State state2 = this.read(uUID, request.binding());
        if (serverPlayer == null || state2 == null || state2.source() != state.source() || state2.principal() != state.principal() || !state2.fingerprint().equals(state.fingerprint())) {
            return null;
        }
        long l = serverPlayer.m_284548_().m_46467_();
        Long l2 = this.lastRequest.get(uUID);
        if (!(FourWeaponTransactions.menuOperation(request.operation()) || l2 == null || l >= l2 && l - l2 >= 4L)) {
            return null;
        }
        this.lastRequest.put(uUID, l);
        final ItemStack itemStack = (ItemStack)state2.source();
        if (!(request.operation() != FourWeaponTransactions.Operation.REPLACE_PART && request.operation() != FourWeaponTransactions.Operation.EVOLVE && request.operation() != FourWeaponTransactions.Operation.BUILD_CELL || !(object = serverPlayer.m_150109_().m_8020_(request.value())).m_41619_() && object != itemStack && ForgeTransactionHost.fingerprint((ItemStack)object).equals(request.offeringFingerprint()))) {
            return null;
        }
        object = new ArrayList();
        for (int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
            object.add(serverPlayer.m_150109_().m_8020_(i).m_41777_());
        }
        ItemStack itemStack2 = serverPlayer.f_36096_.m_142621_().m_41777_();
        CompoundTag compoundTag = itemStack.m_41783_() == null ? null : itemStack.m_41783_().m_6426_();
        AbstractContainerMenu abstractContainerMenu = serverPlayer.f_36096_;
        if (abstractContainerMenu instanceof SwordCavityMenu) {
            swordCavityMenu = (SwordCavityMenu)abstractContainerMenu;
            list = swordCavityMenu.transactionContents();
        } else {
            list = null;
        }
        List<ItemStack> list2 = list;
        swordCavityMenu = BlockPos.m_122022_((long)request.binding().statue());
        return StatueRepairMutation.wrap(new FourWeaponTransactions.Mutation(){
            final /* synthetic */ BlockPos val$pos;
            final /* synthetic */ FourWeaponTransactions.Request val$request;
            final /* synthetic */ CompoundTag val$originalTag;
            final /* synthetic */ List val$inventory;
            final /* synthetic */ AbstractContainerMenu val$originalMenu;
            final /* synthetic */ ItemStack val$carried;
            final /* synthetic */ List val$menuCells;
            {
                this.val$pos = blockPos;
                this.val$request = request;
                this.val$originalTag = compoundTag;
                this.val$inventory = list;
                this.val$originalMenu = abstractContainerMenu;
                this.val$carried = itemStack2;
                this.val$menuCells = list2;
            }

            @Override
            public boolean commit() {
                return ForgeTransactionHost.this.boundary.execute(serverPlayer, itemStack, this.val$pos, this.val$request);
            }

            @Override
            public void rollback() {
                itemStack.m_41751_(this.val$originalTag == null ? null : this.val$originalTag.m_6426_());
                for (int i = 0; i < this.val$inventory.size(); ++i) {
                    if (i == this.val$request.binding().inventorySlot()) continue;
                    serverPlayer.m_150109_().m_6836_(i, ((ItemStack)this.val$inventory.get(i)).m_41777_());
                }
                this.val$originalMenu.m_142503_(this.val$carried.m_41777_());
                if (this.val$menuCells != null && this.val$originalMenu instanceof SwordCavityMenu) {
                    SwordCavityMenu swordCavityMenu = (SwordCavityMenu)this.val$originalMenu;
                    swordCavityMenu.transactionRestore(this.val$menuCells);
                }
                serverPlayer.m_150109_().m_6596_();
            }
        }, serverPlayer, itemStack, request.operation(), request.value(), request.binding().inventorySlot(), (BlockPos)swordCavityMenu, request);
    }

    public static String fingerprint(ItemStack itemStack) {
        return ForgeTransactionHost.fingerprint((Tag)itemStack.serializeNBT());
    }

    public static String fingerprint(Tag tag) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            ForgeTransactionHost.canonical(messageDigest, tag);
            return HexFormat.of().formatHex(messageDigest.digest());
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new IllegalStateException(noSuchAlgorithmException);
        }
    }

    private static void field(MessageDigest messageDigest, String string) {
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        messageDigest.update(ByteBuffer.allocate(4).putInt(byArray.length).array());
        messageDigest.update(byArray);
    }

    private static void canonical(MessageDigest messageDigest, Tag tag) {
        messageDigest.update(tag.m_7060_());
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            List list = compoundTag.m_128431_().stream().sorted().toList();
            ForgeTransactionHost.field(messageDigest, Integer.toString(list.size()));
            for (String string : list) {
                ForgeTransactionHost.field(messageDigest, string);
                ForgeTransactionHost.canonical(messageDigest, Objects.requireNonNull(compoundTag.m_128423_(string)));
            }
        } else if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            ForgeTransactionHost.field(messageDigest, Integer.toString(listTag.size()));
            ForgeTransactionHost.field(messageDigest, Byte.toString(listTag.m_7264_()));
            for (Tag tag2 : listTag) {
                ForgeTransactionHost.canonical(messageDigest, tag2);
            }
        } else {
            ForgeTransactionHost.field(messageDigest, tag.toString());
        }
    }

    public static interface Boundary {
        public boolean verifiedCustomItem(ItemStack var1);

        public String statueIdentity(ServerPlayer var1, BlockPos var2);

        public Policy policy(ServerPlayer var1, ItemStack var2, BlockPos var3);

        public boolean execute(ServerPlayer var1, ItemStack var2, BlockPos var3, FourWeaponTransactions.Request var4);
    }

    public record Policy(int cavityRevision, int repairRevision, int evolutionLevel, String rulesFingerprint, Set<FourWeaponTransactions.Operation> allowed) {
        public Policy {
            set = Set.copyOf(set);
        }
    }
}

