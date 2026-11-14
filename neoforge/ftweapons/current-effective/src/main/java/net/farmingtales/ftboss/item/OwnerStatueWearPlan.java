package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Wear;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;

final class OwnerStatueWearPlan {
    private final int base;
    private final Kind family;
    final int level;
    final String profile;
    CompoundTag cursor;
    private final List<OwnerStatueWearPlan.Step> queue = new ArrayList<>();
    private int next;
    private int cost;
    private boolean started;
    private boolean applied;
    private boolean finishing;
    private boolean finished;
    private CompoundTag rollbackBase;
    volatile boolean rejected;
    boolean rollbackAttempted;
    boolean swallowedRollback;
    int cycles;
    private final net.minecraft.core.HolderLookup.Provider registries;

    private OwnerStatueWearPlan(CompoundTag var1, int var2, Kind var3, int var4, String var5) {
        this(var1, var2, var3, var4, var5, NativeFoundationItemData.registries());
    }

    private OwnerStatueWearPlan(CompoundTag var1, int var2, Kind var3, int var4, String var5, net.minecraft.core.HolderLookup.Provider registries) {
        this.registries = java.util.Objects.requireNonNull(registries, "registries");
        this.cursor = var1.copy();
        this.base = var2;
        this.family = var3;
        this.level = var4;
        this.profile = var5;
    }

    static OwnerStatueWearPlan original(ItemStack var0, CompoundTag var1, int var2) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerStatueWearAdmission.class && var0xx.getMethodName().equals("entered"))
                        .orElse(false)
            );
        return var3 ? ordinary(var0, var1, var2) : null;
    }

    static OwnerStatueWearPlan prewrite(ItemStack var0, CompoundTag var1, int var2) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerRepairContinuation.class && var0xx.getMethodName().equals("entered"))
                        .orElse(false)
            );
        return var3 ? ordinary(var0, var1, var2) : null;
    }

    private static OwnerStatueWearPlan ordinary(ItemStack var0, CompoundTag var1, int var2) {
        if (!var1.contains("ForgeCaps") && !FourWeaponFoundationAdapter.applies(var0) && !SwordCavityBuild.applies(var0)) {
            int var3 = var0.getItem() == Items.BOW ? 384 : (var0.getItem() == Items.CROSSBOW ? 465 : (var0.getItem() == Items.TRIDENT ? 250 : 0));
            if (var3 != 0 && var0.getItem().components().getOrDefault(DataComponents.MAX_DAMAGE, 0) == var3) {
                CompoundTag var4 = NativeRepairStackImage.data(var1);
                CompoundTag var5 = var4.getCompound("FTBossWeaponLifecycle");
                int var6 = Math.max(0, Math.min(4, var4.getCompound("FTBossFoodEvolution").getInt("Level")));
                if (var5.hasUUID("Weapon")
                    && var5.contains("WearStep", 3)
                    && !var5.contains("RepairQuest")
                    && !var5.getString("RepairProfile").isEmpty()
                    && !var5.getBoolean("Broken")
                    && !var0.has(DataComponents.UNBREAKABLE)
                    && var5.getInt("PendingMaterials") == 0
                    && var5.getInt("PendingSockets") == 0
                    && var6 == FoodWeaponEvolution.evolutionLevel(var0)
                    && var5.getString("RepairProfile").equals(FoodWeaponLifecycle.repairProfile(var0))) {
                    OwnerStatueWearPlan var7 = new OwnerStatueWearPlan(
                        var1, var3, var0.getItem() == Items.TRIDENT ? Kind.MELEE : Kind.BOW, var6, var5.getString("RepairProfile")
                    );
                    int var8 = NativeRepairStackImage.damage(var1);
                    return var7.maximum(var1) == var2 && var8 >= 0 && var8 < var2 && var5.getInt("WearStep") >= 0 && var5.getInt("WearStep") <= 4 ? var7 : null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private List<Material> materials(CompoundTag var1) {
        CompoundTag var2 = NativeRepairStackImage.data(var1).getCompound("FTBossWeaponMaterials");
        ArrayList var3 = new ArrayList();

        for (Part var7 : Part.values()) {
            var3.add(Material.byId(var2.getString(var7.name())));
        }

        return List.copyOf(var3);
    }

    private int maximum(CompoundTag var1) {
        return NativeRepairStackImage.data(var1).contains("FTBossWeaponMaterials", 10)
            ? FoodWeaponBuildRules.maxDurability(this.base, FoodWeaponFamilyRules.build(this.family, this.materials(var1)))
            : this.base;
    }

    private static CompoundTag life(CompoundTag var0) {
        return NativeRepairStackImage.data(var0).getCompound("FTBossWeaponLifecycle");
    }

    private static CompoundTag compound(CompoundTag var0, String var1) {
        CompoundTag var2 = NativeRepairStackImage.data(var0);
        return var1.isEmpty() ? var2 : var2.getCompound(var1);
    }

    private boolean complete() {
        return this.next == this.queue.size();
    }

    private void fresh() {
        if (!this.complete()) {
            throw new IllegalStateException("Unfinished independent wear prefix");
        } else {
            this.queue.clear();
            this.next = 0;
        }
    }

    private void append(CompoundTag var1, int var2, String var3, String var4, Tag var5) {
        CompoundTag var6 = compound(var1, var3);
        if (var2 == 8) {
            var1.put("components", var5.copy());
        } else if (var2 == 3 && var3.isEmpty() && var4.equals("Damage")) {
            if (!(var5 instanceof IntTag damage) || damage.getAsInt() < 0) {
                throw new IllegalArgumentException("Invalid native planned wear damage");
            }
            NativeRepairStackImage.putDamage(var1, damage.getAsInt());
        } else if (var2 == 4) {
            var6.getList(var4, 10).remove(var6.getList(var4, 10).size() - 1);
        } else if (var5 == null) {
            var6.remove(var4);
        } else {
            var6.put(var4, var5.copy());
        }

        NativeRepairStackImage.normalize(this.registries, var1);
        this.queue.add(new OwnerStatueWearPlan.Step(var2, var3, var4, var5 == null ? null : var5.copy(), var1.copy()));
    }

    private void ensure(CompoundTag var1) {
        if (this.level > 0) {
            this.append(var1, 2, "FTBossWeaponLifecycle", "WasEvolved", ByteTag.valueOf(true));
        }

        if (NativeRepairStackImage.damage(var1) >= this.maximum(var1)) {
            this.append(var1, 2, "FTBossWeaponLifecycle", "Broken", ByteTag.valueOf(true));
        }
    }

    void cost(int var1) {
        if (!this.rejected && (!this.started || this.finished) && var1 > 0) {
            this.fresh();
            this.started = true;
            this.applied = false;
            this.finishing = false;
            this.finished = false;
            this.rollbackBase = null;
            CompoundTag var2 = this.cursor.copy();
            this.cost = var1;
            if (NativeRepairStackImage.data(var2).contains("FTBossWeaponMaterials", 10)) {
                CompoundTag var3 = NativeRepairStackImage.data(var2).getCompound("FTBossWeaponMaterials");
                Wear var4 = FoodWeaponBuildRules.wear(var1, FoodWeaponFamilyRules.build(this.family, this.materials(var2)).wear(), var3.getDouble("WearCarry"));
                this.cost = var4.damage();
                this.append(var2, 1, "FTBossWeaponMaterials", "WearCarry", DoubleTag.valueOf(var4.carry()));
            }
        } else {
            throw new IllegalStateException("Unsupported original wear cycle");
        }
    }

    void damage(int var1, int var2) {
        if (this.started
            && !this.finished
            && !this.finishing
            && !this.applied
            && var2 >= 0
            && var2 <= this.cost
            && (long)NativeRepairStackImage.damage(this.cursor) + (long)var2 == (long)var1) {
            this.fresh();
            this.applied = true;
            CompoundTag var3 = this.cursor.copy();
            this.ensure(var3);
            int var4 = this.maximum(var3);
            int var5 = Math.max(0, Math.min(var4, var1));
            this.append(var3, 3, "", "Damage", IntTag.valueOf(var5));
            this.rollbackBase = var3.copy();
            int var6 = FoodWeaponLifecycleRules.pendingLosses(life(var3).getInt("WearStep"), var5, var4);
            if (var6 > 0) {
                int var7 = 0;
                int var8 = 0;

                for (int var9 = 0; var9 < var6; var9++) {
                    CompoundTag var10 = NativeRepairStackImage.data(var3).getCompound("FTBossWeaponMaterials");
                    Part[] var11 = Part.values();

                    for (int var12 = var11.length - 1; var12 >= 0; var12--) {
                        if (Material.byId(var10.getString(var11[var12].name())) != Material.ORIGINAL) {
                            this.append(var3, 5, "FTBossWeaponMaterials", var11[var12].name(), StringTag.valueOf(Material.ORIGINAL.id()));
                            var7++;
                            break;
                        }
                    }

                    ListTag var14 = NativeRepairStackImage.data(var3).getCompound("FTBossFoodEvolution").getList("Sockets", 10);
                    if (!var14.isEmpty()) {
                        this.append(var3, 4, "FTBossFoodEvolution", "Sockets", IntTag.valueOf(var14.size() - 1));
                        var8++;
                    }
                }

                int var13 = this.maximum(var3);
                this.append(var3, 3, "", "Damage", IntTag.valueOf(FoodWeaponLifecycleRules.preserveWear(var5, var4, var13)));
                this.append(
                    var3,
                    6,
                    "FTBossWeaponLifecycle",
                    "WearStep",
                    IntTag.valueOf(FoodWeaponLifecycleRules.wearStep(NativeRepairStackImage.damage(var3), var13))
                );
                this.append(var3, 6, "FTBossWeaponLifecycle", "PendingMaterials", IntTag.valueOf(life(var3).getInt("PendingMaterials") + var7));
                this.append(var3, 6, "FTBossWeaponLifecycle", "PendingSockets", IntTag.valueOf(life(var3).getInt("PendingSockets") + var8));
            }

            if (NativeRepairStackImage.damage(var3) >= this.maximum(var3)) {
                this.append(var3, 7, "FTBossWeaponLifecycle", "Broken", ByteTag.valueOf(true));
            }
        } else {
            throw new IllegalStateException("Unbounded vanilla wear request");
        }
    }

    void rollback(CompoundTag var1, Throwable var2) {
        if (this.rollbackBase != null && !this.finishing && var2 != null && this.rollbackBase.getCompound("components").equals(var1)) {
            this.queue.clear();
            this.next = 0;
            this.rollbackAttempted = true;
            CompoundTag var3 = this.cursor.copy();
            this.append(var3, 8, "", "", this.rollbackBase.getCompound("components"));
        } else {
            throw new IllegalStateException("Unknown original rollback baseline");
        }
    }

    void finish(boolean var1) {
        if (this.started && !this.finished && !this.finishing) {
            this.fresh();
            this.finishing = true;
            CompoundTag var2 = this.cursor.copy();
            this.ensure(var2);
            if (var1 && (life(var2).getInt("PendingMaterials") > 0 || life(var2).getInt("PendingSockets") > 0)) {
                this.append(var2, 9, "FTBossWeaponLifecycle", "PendingMaterials", null);
                this.append(var2, 9, "FTBossWeaponLifecycle", "PendingSockets", null);
            }
        } else {
            throw new IllegalStateException("Detached hurt return");
        }
    }

    void finished() {
        if (this.finishing && this.complete()) {
            this.finished = true;
            this.cycles++;
        } else {
            throw new IllegalStateException("Incomplete normal hurt return");
        }
    }

    OwnerStatueWearPlan.Step next(int var1, String var2, Tag var3) {
        if (!this.rejected && !this.complete()) {
            OwnerStatueWearPlan.Step var4 = this.queue.get(this.next);
            if (var4.site == var1 && var4.key.equals(var2) && Objects.equals(var4.value, var3)) {
                return var4;
            } else {
                throw new IllegalStateException("Independent wear operation mismatch");
            }
        } else {
            throw new IllegalStateException("Unplanned wear mutation");
        }
    }

    OwnerStatueWearPlan.Step singleCarryStep() {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0 -> var0.skip(1L)
                        .findFirst()
                        .map(var0x -> var0x.getDeclaringClass() == OwnerRepairContinuation.class && var0x.getMethodName().equals("prewrite"))
                        .orElse(false)
            );
        if (var1 && !this.rejected && this.started && !this.applied && !this.finishing && !this.finished && this.next == 0 && this.queue.size() == 1) {
            OwnerStatueWearPlan.Step var2 = this.queue.get(0);
            CompoundTag var3 = NativeRepairStackImage.data(this.cursor).getCompound("FTBossWeaponMaterials");
            double var4 = var3.getDouble("WearCarry");
            if (var2.site == 1
                && var2.path.equals("FTBossWeaponMaterials")
                && var2.key.equals("WearCarry")
                && var2.value instanceof DoubleTag var6
                && NativeRepairStackImage.data(this.cursor).contains("FTBossWeaponMaterials", 10)
                && Double.isFinite(var4)
                && Double.isFinite(var6.getAsDouble())
                && !DoubleTag.valueOf(var4).equals(var6)
                && !this.cursor.equals(var2.after)) {
                return var2;
            } else {
                throw new IllegalStateException("Absent or unchanged carry");
            }
        } else {
            throw new IllegalStateException("No independent single carry step");
        }
    }

    void advanced(OwnerStatueWearPlan.Step var1) {
        if (var1 != this.queue.get(this.next)) {
            throw new IllegalStateException("Wear step replay");
        } else {
            this.cursor = var1.after.copy();
            this.next++;
        }
    }

    int expectedMaximum() {
        return this.maximum(this.cursor);
    }

    boolean taskStartEligible(Throwable var1) {
        return var1 == null && !this.rollbackAttempted && !this.swallowedRollback && this.terminal(null);
    }

    boolean terminal(Throwable var1) {
        return !this.rejected && this.started && (var1 != null || this.finished && this.complete());
    }

    static record Step(int site, String path, String key, Tag value, CompoundTag after) {
    }
}
