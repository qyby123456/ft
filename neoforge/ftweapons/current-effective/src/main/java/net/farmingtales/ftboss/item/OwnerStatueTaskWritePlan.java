package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

final class OwnerStatueTaskWritePlan {
    final Operation operation;
    final int index;
    final int level;
    final int maximum;
    final String profile;
    final List<OwnerStatueTaskWritePlan.Step> steps = new ArrayList<>();
    CompoundTag cursor;
    int next;
    private boolean submitted;
    private final net.minecraft.core.HolderLookup.Provider registries;

    OwnerStatueTaskWritePlan(CompoundTag var1, Operation var2, int var3, int var4, int var5, String var6) {
        this(var1, var2, var3, var4, var5, var6, NativeFoundationItemData.registries());
    }

    OwnerStatueTaskWritePlan(CompoundTag var1, Operation var2, int var3, int var4, int var5, String var6, net.minecraft.core.HolderLookup.Provider registries) {
        this.registries = java.util.Objects.requireNonNull(registries, "registries");
        this.cursor = var1.copy();
        this.operation = var2;
        this.index = var3;
        this.level = var4;
        this.maximum = var5;
        this.profile = var6;
        CompoundTag var7 = var1.copy();
        CompoundTag var8 = life(var7);
        if (var8.hasUUID("Weapon")
            && var8.contains("WearStep", 3)
            && !var6.isEmpty()
            && var6.equals(var8.getString("RepairProfile"))
            && !var7.contains("ForgeCaps")) {
            if (var2 == Operation.START_REPAIR) {
                if (var8.contains("RepairQuest")) {
                    throw new IllegalArgumentException("Task-origin START already has quest");
                }

                this.ensure(var7);
                CompoundTag var9 = new CompoundTag();
                var9.putInt("Version", 1);
                var9.putInt("Level", var4);
                var9.putString("Profile", var6);
                var9.putIntArray("Progress", new int[3]);
                this.append(var7, "FTBossWeaponLifecycle", "RepairQuest", var9);
                this.revision(var7);
            } else if (var2 == Operation.CLAIM_REPAIR) {
                if (!FoodWeaponLifecycleRules.ready(var8.getCompound("RepairQuest").getIntArray("Progress"), var4)) {
                    throw new IllegalArgumentException("Task-origin claim not ready");
                }

                this.append(var7, "FTBossWeaponLifecycle", "RepairQuest", null);
                this.ensure(var7);
                this.append(var7, "", "Damage", IntTag.valueOf(0));
                this.append(var7, "FTBossWeaponLifecycle", "Broken", null);
                this.append(var7, "FTBossWeaponLifecycle", "PendingMaterials", null);
                this.append(var7, "FTBossWeaponLifecycle", "PendingSockets", null);
                this.append(var7, "FTBossWeaponLifecycle", "WearStep", IntTag.valueOf(0));
                if (NativeRepairStackImage.data(var7).contains("FTBossWeaponMaterials", 10)) {
                    this.append(var7, "FTBossWeaponMaterials", "WearCarry", DoubleTag.valueOf(0.0));
                }

                this.revision(var7);
            } else if (var2 != Operation.SUBMIT_REPAIR) {
                throw new IllegalArgumentException("Not a task operation");
            }
        } else {
            throw new IllegalArgumentException("No task-origin ordinary input");
        }
    }


    OwnerStatueTaskWritePlan(CompoundTag before, Operation operation, java.util.UUID task, int index, int payment, net.minecraft.core.HolderLookup.Provider registries) {
        this.registries = registries;
        this.cursor = before.copy();
        this.operation = operation;
        this.index = index;
        CompoundTag lifecycle = life(before);
        this.level = lifecycle.getCompound("RepairQuest").getInt("Level");
        this.maximum = 0;
        this.profile = lifecycle.getString("RepairProfile");
        CompoundTag after = OwnerStatueTaskPlan.nativeRepair(before, operation, task, index, payment, registries);
        CompoundTag planned = before.copy();
        if (operation == Operation.START_REPAIR) {
            this.append(planned, "FTBossWeaponLifecycle", "RepairQuest", life(after).getCompound("RepairQuest").copy());
            this.revision(planned);
        } else if (operation == Operation.SUBMIT_REPAIR) {
            int[] progress = life(after).getCompound("RepairQuest").getIntArray("Progress");
            this.submitted = true;
            this.steps.add(new OwnerStatueTaskWritePlan.Step("FTBossWeaponLifecycle/RepairQuest", "ProgressArray", new IntArrayTag(progress.clone()), planned.copy()));
            this.append(planned, "FTBossWeaponLifecycle/RepairQuest", "Progress", new IntArrayTag(progress.clone()));
            this.revision(planned);
        } else if (operation == Operation.CLAIM_REPAIR) {
            this.append(planned, "FTBossWeaponLifecycle", "RepairQuest", null);
            this.revision(planned);
            this.append(planned, "", "Damage", IntTag.valueOf(0));
            this.append(planned, "FTBossWeaponLifecycle", "Broken", null);
        } else throw new IllegalArgumentException("Native task write operation");
        if (this.steps.isEmpty() || !this.steps.get(this.steps.size() - 1).after().equals(after)) throw new IllegalStateException("Native task independent plans disagree");
    }

    private static CompoundTag life(CompoundTag var0) {
        return NativeRepairStackImage.data(var0).getCompound("FTBossWeaponLifecycle");
    }

    private void append(CompoundTag var1, String var2, String var3, Tag var4) {
        CompoundTag var5 = NativeRepairStackImage.data(var1);

        for (String var9 : var2.isEmpty() ? new String[0] : var2.split("/")) {
            var5 = var5.getCompound(var9);
        }

        if (var2.isEmpty() && var3.equals("Damage")) {
            if (!(var4 instanceof IntTag damage) || damage.getAsInt() < 0) {
                throw new IllegalArgumentException("Invalid native task-plan damage operation");
            }
            NativeRepairStackImage.putDamage(var1, damage.getAsInt());
        } else if (var4 == null) {
            var5.remove(var3);
        } else {
            var5.put(var3, var4.copy());
        }

        NativeRepairStackImage.normalize(this.registries, var1);
        this.steps.add(new OwnerStatueTaskWritePlan.Step(var2, var3, var4 == null ? null : var4.copy(), var1.copy()));
    }

    private void ensure(CompoundTag var1) {
        if (this.level > 0) {
            this.append(var1, "FTBossWeaponLifecycle", "WasEvolved", ByteTag.valueOf(true));
        }

        if (NativeRepairStackImage.damage(var1) >= this.maximum) {
            this.append(var1, "FTBossWeaponLifecycle", "Broken", ByteTag.valueOf(true));
        }
    }

    private void revision(CompoundTag var1) {
        int var2 = Math.max(0, life(var1).getInt("RepairRevision"));
        this.append(var1, "FTBossWeaponLifecycle", "RepairRevision", IntTag.valueOf(var2 == Integer.MAX_VALUE ? 0 : var2 + 1));
    }

    void submit(int var1) {
        if (this.operation == Operation.SUBMIT_REPAIR && !this.submitted && var1 > 0 && this.index >= 0 && this.index < 3) {
            int[] var2 = life(this.cursor).getCompound("RepairQuest").getIntArray("Progress");
            if (var2.length == 3 && var1 <= FoodWeaponLifecycleRules.repairRequired(this.level, this.index) - var2[this.index]) {
                this.submitted = true;
                var2[this.index] = Math.addExact(var2[this.index], var1);
                CompoundTag var3 = this.cursor.copy();
                this.append(var3, "FTBossWeaponLifecycle/RepairQuest", "Progress", new IntArrayTag(var2));
                OwnerStatueTaskWritePlan.Step var4 = this.steps.remove(this.steps.size() - 1);
                this.steps.add(new OwnerStatueTaskWritePlan.Step(var4.path(), "ProgressArray", var4.value(), var4.after()));
                this.append(var3, "FTBossWeaponLifecycle/RepairQuest", "Progress", new IntArrayTag(var2));
                this.revision(var3);
            } else {
                throw new IllegalArgumentException("Task payment exceeds independent need");
            }
        } else {
            throw new IllegalArgumentException("Unverified task payment");
        }
    }

    OwnerStatueTaskWritePlan.Step expected(String var1, Tag var2) {
        if (this.next >= this.steps.size()) {
            throw new IllegalStateException("Unplanned task write");
        } else {
            OwnerStatueTaskWritePlan.Step var3 = this.steps.get(this.next);
            if (var3.key.equals(var1) && Objects.equals(var3.value, var2)) {
                return var3;
            } else {
                throw new IllegalStateException("Independent task write mismatch");
            }
        }
    }

    void advance(OwnerStatueTaskWritePlan.Step var1) {
        if (var1 != this.steps.get(this.next)) {
            throw new IllegalStateException("Task step replay");
        } else {
            this.cursor = var1.after.copy();
            this.next++;
        }
    }

    boolean complete() {
        return this.next == this.steps.size() && (this.operation != Operation.SUBMIT_REPAIR || this.submitted);
    }

    static record Step(String path, String key, Tag value, CompoundTag after) {
    }
}
