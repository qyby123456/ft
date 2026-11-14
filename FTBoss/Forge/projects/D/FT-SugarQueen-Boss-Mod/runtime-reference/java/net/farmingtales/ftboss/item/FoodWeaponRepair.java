/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.OwnerStatueTaskWrites;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FoodWeaponRepair {
    private static final String QUEST = "RepairQuest";
    private static final String REVISION = "RepairRevision";
    private static final int QUEST_VERSION = 1;

    private FoodWeaponRepair() {
    }

    public static int revision(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle");
        return compoundTag == null ? 0 : Math.max(0, compoundTag.m_128451_(REVISION));
    }

    private static CompoundTag activeQuest(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle");
        CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.m_128469_(QUEST);
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        if (compoundTag2.m_128451_("Version") != 1 || compoundTag2.m_128451_("Level") != n || !FoodWeaponLifecycle.repairProfile(itemStack).equals(compoundTag2.m_128461_("Profile"))) {
            return new CompoundTag();
        }
        int[] nArray = compoundTag2.m_128465_("Progress");
        if (nArray.length != 3) {
            return new CompoundTag();
        }
        for (int i = 0; i < nArray.length; ++i) {
            if (nArray[i] >= 0 && nArray[i] <= FoodWeaponLifecycleRules.repairRequired(n, i)) continue;
            return new CompoundTag();
        }
        return compoundTag2;
    }

    private static void advance(ItemStack itemStack) {
        itemStack.m_41698_("FTBossWeaponLifecycle").m_128405_(REVISION, FoodWeaponRepair.revision(itemStack) == Integer.MAX_VALUE ? 0 : FoodWeaponRepair.revision(itemStack) + 1);
    }

    static void invalidateForProgression(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle");
        if (compoundTag == null || !compoundTag.m_128441_(QUEST)) {
            return;
        }
        compoundTag.m_128473_(QUEST);
        FoodWeaponRepair.advance(itemStack);
    }

    public static View view(ItemStack itemStack, int n) {
        String string = FoodWeaponLifecycle.repairProfile(itemStack);
        boolean bl = !string.isEmpty() && string.equals(FoodWeaponEvolution.profileId(n));
        boolean bl2 = NativeFoodWeaponWear.eligible(itemStack) && itemStack.m_41773_() > 0;
        CompoundTag compoundTag = FoodWeaponRepair.activeQuest(itemStack);
        boolean bl3 = !compoundTag.m_128456_();
        int n2 = FoodWeaponEvolution.evolutionLevel(itemStack);
        int[] nArray = bl3 ? OwnerStatueTaskWrites.progress(itemStack, compoundTag) : new int[3];
        ArrayList<TaskView> arrayList = new ArrayList<TaskView>();
        int n3 = 0;
        for (int i = 0; i < 3; ++i) {
            boolean bl4;
            int n4 = FoodWeaponLifecycleRules.repairRequired(n2, i);
            Component component = i == 0 ? Component.m_237110_((String)"screen.ftboss.repair.food", (Object[])new Object[]{Component.m_237115_((String)("profile.ftboss.evolution." + string))}) : (i == 1 ? Items.f_42416_ : Items.f_42534_).m_41466_();
            boolean bl5 = bl4 = nArray[i] == n4;
            if (bl4) {
                ++n3;
            }
            arrayList.add(new TaskView(i, component, nArray[i], n4, bl4));
        }
        return new View(bl2, bl, bl3, bl3 && FoodWeaponLifecycleRules.ready(nArray, n2), n3, 3, arrayList);
    }

    private static boolean valid(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        View view = FoodWeaponRepair.view(itemStack, n);
        return !serverPlayer.m_9236_().f_46443_ && serverPlayer.m_20194_() != null && serverPlayer.m_20194_().m_18695_() && serverPlayer.m_6084_() && !serverPlayer.m_5833_() && view.eligible && view.correctStatue;
    }

    public static boolean start(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        if (!FoodWeaponRepair.valid(serverPlayer, itemStack, n) || FoodWeaponRepair.view((ItemStack)itemStack, (int)n).active) {
            return false;
        }
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("Version", 1);
        if (!NativeFoodWeaponWear.startTask(serverPlayer, itemStack, compoundTag)) {
            return false;
        }
        compoundTag.m_128405_("Level", FoodWeaponEvolution.evolutionLevel(itemStack));
        compoundTag.m_128359_("Profile", FoodWeaponLifecycle.repairProfile(itemStack));
        compoundTag.m_128385_("Progress", new int[3]);
        OwnerStatueTaskWrites.put(itemStack, itemStack.m_41698_("FTBossWeaponLifecycle"), QUEST, (Tag)compoundTag);
        FoodWeaponRepair.advance(itemStack);
        serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.repair.started"), true);
        return true;
    }

    public static boolean submit(ServerPlayer serverPlayer, ItemStack itemStack, int n, int n2) {
        if (!FoodWeaponRepair.valid(serverPlayer, itemStack, n) || n2 < 0 || n2 >= 3) {
            return false;
        }
        CompoundTag compoundTag = FoodWeaponRepair.activeQuest(itemStack);
        if (compoundTag.m_128456_() || !NativeFoodWeaponWear.taskLive(serverPlayer, itemStack, compoundTag)) {
            return false;
        }
        int[] nArray = OwnerStatueTaskWrites.progress(itemStack, compoundTag);
        int n3 = FoodWeaponLifecycleRules.repairRequired(compoundTag.m_128451_("Level"), n2);
        int n4 = n3 - nArray[n2];
        if (n4 <= 0) {
            return false;
        }
        int n5 = 0;
        for (int i = 0; i < 36 && n5 < n4; ++i) {
            boolean bl;
            ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(i);
            if (itemStack2.m_41619_() || itemStack2 == itemStack || FoodWeaponEvolution.isEvolutionCandidate(itemStack2)) continue;
            boolean bl2 = n2 == 0 ? FoodWeaponEvolution.matchesStatueFood(itemStack2, n) : (bl = itemStack2.m_150930_(n2 == 1 ? Items.f_42416_ : Items.f_42534_));
            if (!bl) continue;
            int n6 = Math.min(itemStack2.m_41613_(), n4 - n5);
            StatueRepairMutation.paymentBefore(serverPlayer, itemStack, itemStack2, n2, i, n6);
            itemStack2.m_41774_(n6);
            StatueRepairMutation.paymentAfter(serverPlayer, itemStack, itemStack2, n2, i, n6);
            n5 += n6;
        }
        if (n5 == 0) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.repair.supply_required"), true);
            return false;
        }
        NativeFoodWeaponWear.recordTaskPayment(serverPlayer, itemStack, compoundTag, n2, n5);
        OwnerStatueTaskWrites.advanceProgress(itemStack, compoundTag, nArray, n2, n5);
        OwnerStatueTaskWrites.putIntArray(itemStack, compoundTag, "Progress", nArray);
        FoodWeaponRepair.advance(itemStack);
        serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.repair.submitted", (Object[])new Object[]{n5}), true);
        return true;
    }

    public static boolean claim(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        if (!FoodWeaponRepair.valid(serverPlayer, itemStack, n) || !FoodWeaponRepair.view((ItemStack)itemStack, (int)n).ready) {
            return false;
        }
        if (!NativeFoodWeaponWear.prepareStatueClaim(serverPlayer, itemStack, FoodWeaponRepair.activeQuest(itemStack))) {
            return false;
        }
        OwnerStatueTaskWrites.remove(itemStack, itemStack.m_41698_("FTBossWeaponLifecycle"), QUEST);
        FoodWeaponRepair.advance(itemStack);
        serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_11671_, SoundSource.PLAYERS, 0.7f, 1.0f);
        serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.repair.complete"), true);
        FTBossMod.LOGGER.info("[FTBoss][\u98df\u6b66\u77f3\u50cf\u4fee\u590d] {} weapon={} profile={} max={}", new Object[]{serverPlayer.m_7755_().getString(), FoodWeaponLifecycle.weaponId(itemStack), FoodWeaponLifecycle.repairProfile(itemStack), itemStack.m_41776_()});
        return true;
    }

    public record TaskView(int index, Component name, int progress, int required, boolean complete) {
    }

    public record View(boolean eligible, boolean correctStatue, boolean active, boolean ready, int completed, int total, List<TaskView> tasks) {
        public View {
            list = List.copyOf(list);
        }
    }
}

