/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.transaction;

import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponUiSnapshot {
    private final UUID request;
    private final CompoundTag data;

    public WeaponUiSnapshot(UUID uUID, CompoundTag compoundTag) {
        this.request = Objects.requireNonNull(uUID);
        this.data = compoundTag.m_6426_();
        if (!this.data.m_128403_("Session") || this.data.m_128437_("Cells", 10).size() != 27 || this.data.m_128437_("Axes", 10).size() != 5) {
            throw new IllegalArgumentException("UI snapshot shape");
        }
    }

    public UUID request() {
        return this.request;
    }

    public UUID session() {
        return this.data.m_128342_("Session");
    }

    public CompoundTag data() {
        return this.data.m_6426_();
    }

    public static WeaponUiSnapshot read(ServerPlayer serverPlayer, FourWeaponTransactions.Reply reply) {
        CompoundTag compoundTag;
        Object object2;
        FourWeaponTransactions.Snapshot snapshot = reply.snapshot();
        if (snapshot == null) {
            return null;
        }
        ItemStack itemStack = serverPlayer.m_150109_().m_8020_(snapshot.binding().inventorySlot());
        if (!snapshot.binding().weapon().equals(FoodWeaponLifecycle.weaponId(itemStack)) || !snapshot.version().fingerprint().equals(ForgeTransactionHost.fingerprint(itemStack))) {
            return null;
        }
        FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid()) {
            return null;
        }
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128362_("Session", snapshot.session());
        compoundTag2.m_128362_("Weapon", snapshot.binding().weapon());
        compoundTag2.m_128356_("Sequence", snapshot.version().sequence());
        compoundTag2.m_128405_("GridRevision", view.revision());
        compoundTag2.m_128405_("Level", view.level());
        compoundTag2.m_128359_("Family", view.profile().family());
        compoundTag2.m_128359_("Fingerprint", snapshot.version().fingerprint());
        compoundTag2.m_128359_("Rules", snapshot.version().rulesFingerprint());
        compoundTag2.m_128379_("Usable", view.status().ready());
        compoundTag2.m_128385_("Layout", SwordCavityExtensions.layout(itemStack));
        ListTag listTag = new ListTag();
        for (int i = 0; i < 5; ++i) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128359_("Id", view.profile().axes().get(i));
            compoundTag3.m_128347_("Total", view.status().total().at(i));
            compoundTag3.m_128347_("Position", view.status().position().at(i));
            compoundTag3.m_128347_("Material", view.status().material().at(i));
            compoundTag3.m_128347_("Minimum", view.profile().minimum().at(i));
            listTag.add((Object)compoundTag3);
        }
        compoundTag2.m_128365_("Axes", (Tag)listTag);
        ListTag listTag2 = new ListTag();
        for (int i = 0; i < 27; ++i) {
            CompoundTag compoundTag4 = new CompoundTag();
            compoundTag4.m_128405_("Slot", i);
            compoundTag4.m_128379_("Unlocked", FourWeaponFoundationAdapter.cellUnlocked(itemStack, i));
            object2 = view.entries().get(i);
            compoundTag4.m_128379_("Paid", object2 != null && ((FourWeaponFoundationAdapter.Entry)object2).paid());
            compoundTag4.m_128379_("Native", object2 != null && !((FourWeaponFoundationAdapter.Entry)object2).paid());
            if (object2 != null) {
                compoundTag4.m_128359_("ItemId", ((FourWeaponFoundationAdapter.Entry)object2).value().itemId());
                compoundTag4.m_128359_("Material", ((FourWeaponFoundationAdapter.Entry)object2).value().material().id());
                if (((FourWeaponFoundationAdapter.Entry)object2).paid()) {
                    compoundTag4.m_128365_("Item", (Tag)((FourWeaponFoundationAdapter.Entry)object2).payload());
                }
            }
            listTag2.add((Object)compoundTag4);
        }
        compoundTag2.m_128365_("Cells", (Tag)listTag2);
        ListTag listTag3 = new ListTag();
        for (int i = 0; i < 36; ++i) {
            object2 = serverPlayer.m_150109_().m_8020_(i);
            if (object2.m_41619_() || object2 == itemStack) continue;
            CompoundTag object3 = new CompoundTag();
            object3.m_128405_("Slot", i);
            object3.m_128359_("ItemId", FourWeaponFoundationAdapter.registryId((ItemStack)object2));
            object3.m_128359_("Fingerprint", ForgeTransactionHost.fingerprint((ItemStack)object2));
            object3.m_128405_("Count", object2.m_41613_());
            listTag3.add((Object)object3);
        }
        compoundTag2.m_128365_("Supplies", (Tag)listTag3);
        ListTag listTag4 = new ListTag();
        for (FoodWeaponEvolution.WeaponDetailView weaponDetailView : FoodWeaponSkillLoadout.skills(itemStack)) {
            compoundTag = new CompoundTag();
            compoundTag.m_128405_("Index", weaponDetailView.gourmetSkillIndex());
            compoundTag.m_128379_("Selected", weaponDetailView.selected());
            compoundTag.m_128359_("Title", Component.Serializer.m_130703_((Component)weaponDetailView.title()));
            compoundTag.m_128359_("Description", Component.Serializer.m_130703_((Component)weaponDetailView.description()));
            listTag4.add((Object)compoundTag);
        }
        compoundTag2.m_128365_("Skills", (Tag)listTag4);
        int n = Integer.parseInt(snapshot.binding().statueIdentity().substring(snapshot.binding().statueIdentity().lastIndexOf(58) + 1));
        FoodWeaponRepair.View view2 = FoodWeaponRepair.view(itemStack, n);
        compoundTag = new CompoundTag();
        compoundTag.m_128379_("Eligible", view2.eligible());
        compoundTag.m_128379_("Active", view2.active());
        compoundTag.m_128379_("Ready", view2.ready());
        compoundTag.m_128379_("CorrectStatue", view2.correctStatue());
        ListTag listTag5 = new ListTag();
        for (FoodWeaponRepair.TaskView taskView : view2.tasks()) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128405_("Index", taskView.index());
            compoundTag3.m_128405_("Progress", taskView.progress());
            compoundTag3.m_128405_("Required", taskView.required());
            compoundTag3.m_128359_("Name", Component.Serializer.m_130703_((Component)taskView.name()));
            listTag5.add((Object)compoundTag3);
        }
        compoundTag.m_128365_("Tasks", (Tag)listTag5);
        compoundTag2.m_128365_("Repair", (Tag)compoundTag);
        compoundTag2.m_128405_("Container", serverPlayer.f_36096_.f_38840_);
        compoundTag2.m_128365_("Cursor", (Tag)serverPlayer.f_36096_.m_142621_().m_41739_(new CompoundTag()));
        return new WeaponUiSnapshot(reply.request(), compoundTag2);
    }
}

