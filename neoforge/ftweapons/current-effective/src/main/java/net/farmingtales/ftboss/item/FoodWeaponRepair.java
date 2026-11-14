package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.CompoundTag;
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

    public static int revision(ItemStack var0) {
        CompoundTag var1 = NativeRepairTaskData.read(var0);
        return var1 == null ? 0 : Math.max(0, var1.getInt("RepairRevision"));
    }

    private static CompoundTag activeQuest(ItemStack var0) {
        CompoundTag var1 = NativeRepairTaskData.read(var0);
        CompoundTag var2 = var1 == null ? new CompoundTag() : var1.getCompound("RepairQuest");
        int var3 = FoodWeaponEvolution.evolutionLevel(var0);
        if (var2.getInt("Version") == 1 && var2.getInt("Level") == var3 && FoodWeaponLifecycle.repairProfile(var0).equals(var2.getString("Profile"))) {
            int[] var4 = OwnerStatueTaskWrites.progress(var0, var2);
            if (var4.length != 3) {
                return new CompoundTag();
            } else {
                for (int var5 = 0; var5 < var4.length; var5++) {
                    if (var4[var5] < 0 || var4[var5] > FoodWeaponLifecycleRules.repairRequired(var3, var5)) {
                        return new CompoundTag();
                    }
                }

                return var2;
            }
        } else {
            return new CompoundTag();
        }
    }

    private static void advance(ItemStack var0) {
        OwnerStatueTaskWrites.putInt(var0, NativeRepairTaskData.ensure(var0), "RepairRevision", revision(var0) == Integer.MAX_VALUE ? 0 : revision(var0) + 1);
    }

    static void invalidateForProgression(ItemStack var0) {
        CompoundTag var1 = NativeRepairTaskData.read(var0);
        if (var1 != null && var1.contains("RepairQuest")) {
            var1.remove("RepairQuest");
            advance(var0);
        }
    }

    public static FoodWeaponRepair.View view(ItemStack var0, int var1) {
        String var2 = FoodWeaponLifecycle.repairProfile(var0);
        boolean var3 = !var2.isEmpty() && var2.equals(FoodWeaponEvolution.profileId(var1));
        boolean var4 = NativeFoodWeaponWear.eligible(var0) && var0.getDamageValue() > 0;
        CompoundTag var5 = activeQuest(var0);
        boolean var6 = !var5.isEmpty();
        int var7 = FoodWeaponEvolution.evolutionLevel(var0);
        int[] var8 = var6 ? OwnerStatueTaskWrites.progress(var0, var5) : new int[3];
        ArrayList var9 = new ArrayList();
        int var10 = 0;

        for (int var11 = 0; var11 < 3; var11++) {
            int var12 = FoodWeaponLifecycleRules.repairRequired(var7, var11);
            Object var13 = var11 == 0
                ? Component.translatable("screen.ftboss.repair.food", Component.translatable("profile.ftboss.evolution." + var2))
                : (var11 == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI).getDescription();
            boolean var14 = var8[var11] == var12;
            if (var14) {
                var10++;
            }

            var9.add(new FoodWeaponRepair.TaskView(var11, (Component)var13, var8[var11], var12, var14));
        }

        return new FoodWeaponRepair.View(var4, var3, var6, var6 && FoodWeaponLifecycleRules.ready(var8, var7), var10, 3, var9);
    }

    private static boolean valid(ServerPlayer var0, ItemStack var1, int var2) {
        FoodWeaponRepair.View var3 = view(var1, var2);
        return !var0.level().isClientSide
            && var0.getServer() != null
            && var0.getServer().isSameThread()
            && var0.isAlive()
            && !var0.isSpectator()
            && var3.eligible
            && var3.correctStatue;
    }

    public static boolean start(ServerPlayer var0, ItemStack var1, int var2) {
        if (valid(var0, var1, var2) && !view(var1, var2).active) {
            FoodWeaponLifecycle.ensureTracked(var1);
            CompoundTag var3 = new CompoundTag();
            var3.putInt("Version", 1);
            if (!NativeFoodWeaponWear.startTask(var0, var1, var3)) {
                return false;
            } else {
                var3.putInt("Level", FoodWeaponEvolution.evolutionLevel(var1));
                var3.putString("Profile", FoodWeaponLifecycle.repairProfile(var1));
                var3.putIntArray("Progress", new int[3]);
                OwnerStatueTaskWrites.put(var1, NativeRepairTaskData.ensure(var1), "RepairQuest", var3);
                advance(var1);
                var0.displayClientMessage(Component.translatable("message.ftboss.repair.started"), true);
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean submit(ServerPlayer var0, ItemStack var1, int var2, int var3) {
        if (valid(var0, var1, var2) && var3 >= 0 && var3 < 3) {
            CompoundTag var4 = activeQuest(var1);
            if (!var4.isEmpty() && NativeFoodWeaponWear.taskLive(var0, var1, var4)) {
                int[] var5 = OwnerStatueTaskWrites.progress(var1, var4);
                int var6 = FoodWeaponLifecycleRules.repairRequired(var4.getInt("Level"), var3);
                int var7 = var6 - var5[var3];
                if (var7 <= 0) {
                    return false;
                } else {
                    int var8 = 0;

                    for (int var9 = 0; var9 < 36 && var8 < var7; var9++) {
                        ItemStack var10 = var0.getInventory().getItem(var9);
                        if (!var10.isEmpty() && var10 != var1 && !FoodWeaponEvolution.isEvolutionCandidate(var10)) {
                            boolean var11 = var3 == 0
                                ? FoodWeaponEvolution.matchesStatueFood(var10, var2)
                                : var10.is(var3 == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI);
                            if (var11) {
                                int var12 = Math.min(var10.getCount(), var7 - var8);
                                StatueRepairMutation.paymentBefore(var0, var1, var10, var3, var9, var12);
                                var10.shrink(var12);
                                StatueRepairMutation.paymentAfter(var0, var1, var10, var3, var9, var12);
                                var8 += var12;
                            }
                        }
                    }

                    if (var8 == 0) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.repair.supply_required"), true);
                        return false;
                    } else {
                        NativeFoodWeaponWear.recordTaskPayment(var0, var1, var4, var3, var8);
                        OwnerStatueTaskWrites.advanceProgress(var1, var4, var5, var3, var8);
                        OwnerStatueTaskWrites.putIntArray(var1, var4, "Progress", var5);
                        advance(var1);
                        var0.displayClientMessage(Component.translatable("message.ftboss.repair.submitted", var8), true);
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean claim(ServerPlayer var0, ItemStack var1, int var2) {
        if (!valid(var0, var1, var2) || !view(var1, var2).ready) {
            return false;
        } else if (!NativeFoodWeaponWear.prepareStatueClaim(var0, var1, activeQuest(var1))) {
            return false;
        } else {
            OwnerStatueTaskWrites.remove(var1, NativeRepairTaskData.ensure(var1), "RepairQuest");
            advance(var1);
            var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.7F, 1.0F);
            var0.displayClientMessage(Component.translatable("message.ftboss.repair.complete"), true);
            FTBossMod.LOGGER
                .info(
                    "[FTBoss][食武石像修复] {} weapon={} profile={} max={}",
                    new Object[]{var0.getName().getString(), FoodWeaponLifecycle.weaponId(var1), FoodWeaponLifecycle.repairProfile(var1), var1.getMaxDamage()}
                );
            return true;
        }
    }

    public static record TaskView(int index, Component name, int progress, int required, boolean complete) {
    }

    public static record View(
        boolean eligible, boolean correctStatue, boolean active, boolean ready, int completed, int total, List<FoodWeaponRepair.TaskView> tasks
    ) {
        public View(boolean eligible, boolean correctStatue, boolean active, boolean ready, int completed, int total, List<FoodWeaponRepair.TaskView> tasks) {
            tasks = List.copyOf(tasks);
            this.eligible = eligible;
            this.correctStatue = correctStatue;
            this.active = active;
            this.ready = ready;
            this.completed = completed;
            this.total = total;
            this.tasks = tasks;
        }
    }
}
