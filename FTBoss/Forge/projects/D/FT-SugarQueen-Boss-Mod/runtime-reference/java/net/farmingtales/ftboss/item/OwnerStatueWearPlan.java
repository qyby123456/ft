/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class OwnerStatueWearPlan {
    private final int base;
    private final FoodWeaponFamilyRules.Kind family;
    final int level;
    final String profile;
    CompoundTag cursor;
    private final List<Step> queue = new ArrayList<Step>();
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

    private OwnerStatueWearPlan(CompoundTag compoundTag, int n, FoodWeaponFamilyRules.Kind kind, int n2, String string) {
        this.cursor = compoundTag.m_6426_();
        this.base = n;
        this.family = kind;
        this.level = n2;
        this.profile = string;
    }

    static OwnerStatueWearPlan original(ItemStack itemStack, CompoundTag compoundTag, int n) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueWearAdmission.class && stackFrame.getMethodName().equals("entered")).orElse(false));
        return bl ? OwnerStatueWearPlan.ordinary(itemStack, compoundTag, n) : null;
    }

    static OwnerStatueWearPlan prewrite(ItemStack itemStack, CompoundTag compoundTag, int n) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerRepairContinuation.class && stackFrame.getMethodName().equals("entered")).orElse(false));
        return bl ? OwnerStatueWearPlan.ordinary(itemStack, compoundTag, n) : null;
    }

    private static OwnerStatueWearPlan ordinary(ItemStack itemStack, CompoundTag compoundTag, int n) {
        int n2;
        if (compoundTag.m_128441_("ForgeCaps") || FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack)) {
            return null;
        }
        int n3 = itemStack.m_41720_() == Items.f_42411_ ? 384 : (itemStack.m_41720_() == Items.f_42717_ ? 465 : (n2 = itemStack.m_41720_() == Items.f_42713_ ? 250 : 0));
        if (n2 == 0 || itemStack.m_41720_().m_41462_() != n2) {
            return null;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("tag");
        CompoundTag compoundTag3 = compoundTag2.m_128469_("FTBossWeaponLifecycle");
        int n4 = Math.max(0, Math.min(4, compoundTag2.m_128469_("FTBossFoodEvolution").m_128451_("Level")));
        if (!compoundTag3.m_128403_("Weapon") || !compoundTag3.m_128425_("WearStep", 3) || compoundTag3.m_128441_("RepairQuest") || compoundTag3.m_128461_("RepairProfile").isEmpty() || compoundTag3.m_128471_("Broken") || compoundTag2.m_128471_("Unbreakable") || compoundTag3.m_128451_("PendingMaterials") != 0 || compoundTag3.m_128451_("PendingSockets") != 0 || n4 != FoodWeaponEvolution.evolutionLevel(itemStack) || !compoundTag3.m_128461_("RepairProfile").equals(FoodWeaponLifecycle.repairProfile(itemStack))) {
            return null;
        }
        OwnerStatueWearPlan ownerStatueWearPlan = new OwnerStatueWearPlan(compoundTag, n2, itemStack.m_41720_() == Items.f_42713_ ? FoodWeaponFamilyRules.Kind.MELEE : FoodWeaponFamilyRules.Kind.BOW, n4, compoundTag3.m_128461_("RepairProfile"));
        int n5 = compoundTag2.m_128451_("Damage");
        if (ownerStatueWearPlan.maximum(compoundTag) != n || n5 < 0 || n5 >= n || compoundTag3.m_128451_("WearStep") < 0 || compoundTag3.m_128451_("WearStep") > 4) {
            return null;
        }
        return ownerStatueWearPlan;
    }

    private List<FoodWeaponBuildRules.Material> materials(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag.m_128469_("tag").m_128469_("FTBossWeaponMaterials");
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>();
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            arrayList.add(FoodWeaponBuildRules.Material.byId(compoundTag2.m_128461_(part.name())));
        }
        return List.copyOf(arrayList);
    }

    private int maximum(CompoundTag compoundTag) {
        return compoundTag.m_128469_("tag").m_128425_("FTBossWeaponMaterials", 10) ? FoodWeaponBuildRules.maxDurability(this.base, FoodWeaponFamilyRules.build(this.family, this.materials(compoundTag))) : this.base;
    }

    private static CompoundTag life(CompoundTag compoundTag) {
        return compoundTag.m_128469_("tag").m_128469_("FTBossWeaponLifecycle");
    }

    private static CompoundTag compound(CompoundTag compoundTag, String string) {
        CompoundTag compoundTag2 = compoundTag.m_128469_("tag");
        return string.isEmpty() ? compoundTag2 : compoundTag2.m_128469_(string);
    }

    private boolean complete() {
        return this.next == this.queue.size();
    }

    private void fresh() {
        if (!this.complete()) {
            throw new IllegalStateException("Unfinished independent wear prefix");
        }
        this.queue.clear();
        this.next = 0;
    }

    private void append(CompoundTag compoundTag, int n, String string, String string2, Tag tag) {
        CompoundTag compoundTag2 = OwnerStatueWearPlan.compound(compoundTag, string);
        if (n == 8) {
            compoundTag.m_128365_("tag", tag.m_6426_());
        } else if (n == 4) {
            compoundTag2.m_128437_(string2, 10).remove(compoundTag2.m_128437_(string2, 10).size() - 1);
        } else if (tag == null) {
            compoundTag2.m_128473_(string2);
        } else {
            compoundTag2.m_128365_(string2, tag.m_6426_());
        }
        this.queue.add(new Step(n, string, string2, tag == null ? null : tag.m_6426_(), compoundTag.m_6426_()));
    }

    private void ensure(CompoundTag compoundTag) {
        if (this.level > 0) {
            this.append(compoundTag, 2, "FTBossWeaponLifecycle", "WasEvolved", (Tag)ByteTag.m_128273_((boolean)true));
        }
        if (compoundTag.m_128469_("tag").m_128451_("Damage") >= this.maximum(compoundTag)) {
            this.append(compoundTag, 2, "FTBossWeaponLifecycle", "Broken", (Tag)ByteTag.m_128273_((boolean)true));
        }
    }

    void cost(int n) {
        if (this.rejected || this.started && !this.finished || n <= 0) {
            throw new IllegalStateException("Unsupported original wear cycle");
        }
        this.fresh();
        this.started = true;
        this.applied = false;
        this.finishing = false;
        this.finished = false;
        this.rollbackBase = null;
        CompoundTag compoundTag = this.cursor.m_6426_();
        this.cost = n;
        if (compoundTag.m_128469_("tag").m_128425_("FTBossWeaponMaterials", 10)) {
            CompoundTag compoundTag2 = compoundTag.m_128469_("tag").m_128469_("FTBossWeaponMaterials");
            FoodWeaponBuildRules.Wear wear = FoodWeaponBuildRules.wear(n, FoodWeaponFamilyRules.build(this.family, this.materials(compoundTag)).wear(), compoundTag2.m_128459_("WearCarry"));
            this.cost = wear.damage();
            this.append(compoundTag, 1, "FTBossWeaponMaterials", "WearCarry", (Tag)DoubleTag.m_128500_((double)wear.carry()));
        }
    }

    void damage(int n, int n2) {
        if (!this.started || this.finished || this.finishing || this.applied || n2 < 0 || n2 > this.cost || (long)this.cursor.m_128469_("tag").m_128451_("Damage") + (long)n2 != (long)n) {
            throw new IllegalStateException("Unbounded vanilla wear request");
        }
        this.fresh();
        this.applied = true;
        CompoundTag compoundTag = this.cursor.m_6426_();
        this.ensure(compoundTag);
        int n3 = this.maximum(compoundTag);
        int n4 = Math.max(0, Math.min(n3, n));
        this.append(compoundTag, 3, "", "Damage", (Tag)IntTag.m_128679_((int)n4));
        this.rollbackBase = compoundTag.m_6426_();
        int n5 = FoodWeaponLifecycleRules.pendingLosses(OwnerStatueWearPlan.life(compoundTag).m_128451_("WearStep"), n4, n3);
        if (n5 > 0) {
            int n6;
            int n7 = 0;
            int n8 = 0;
            for (n6 = 0; n6 < n5; ++n6) {
                ListTag listTag;
                CompoundTag compoundTag2 = compoundTag.m_128469_("tag").m_128469_("FTBossWeaponMaterials");
                FoodWeaponBuildRules.Part[] partArray = FoodWeaponBuildRules.Part.values();
                for (int i = partArray.length - 1; i >= 0; --i) {
                    if (FoodWeaponBuildRules.Material.byId(compoundTag2.m_128461_(partArray[i].name())) == FoodWeaponBuildRules.Material.ORIGINAL) continue;
                    this.append(compoundTag, 5, "FTBossWeaponMaterials", partArray[i].name(), (Tag)StringTag.m_129297_((String)FoodWeaponBuildRules.Material.ORIGINAL.id()));
                    ++n7;
                    break;
                }
                if ((listTag = compoundTag.m_128469_("tag").m_128469_("FTBossFoodEvolution").m_128437_("Sockets", 10)).isEmpty()) continue;
                this.append(compoundTag, 4, "FTBossFoodEvolution", "Sockets", (Tag)IntTag.m_128679_((int)(listTag.size() - 1)));
                ++n8;
            }
            n6 = this.maximum(compoundTag);
            this.append(compoundTag, 3, "", "Damage", (Tag)IntTag.m_128679_((int)FoodWeaponLifecycleRules.preserveWear(n4, n3, n6)));
            this.append(compoundTag, 6, "FTBossWeaponLifecycle", "WearStep", (Tag)IntTag.m_128679_((int)FoodWeaponLifecycleRules.wearStep(compoundTag.m_128469_("tag").m_128451_("Damage"), n6)));
            this.append(compoundTag, 6, "FTBossWeaponLifecycle", "PendingMaterials", (Tag)IntTag.m_128679_((int)(OwnerStatueWearPlan.life(compoundTag).m_128451_("PendingMaterials") + n7)));
            this.append(compoundTag, 6, "FTBossWeaponLifecycle", "PendingSockets", (Tag)IntTag.m_128679_((int)(OwnerStatueWearPlan.life(compoundTag).m_128451_("PendingSockets") + n8)));
        }
        if (compoundTag.m_128469_("tag").m_128451_("Damage") >= this.maximum(compoundTag)) {
            this.append(compoundTag, 7, "FTBossWeaponLifecycle", "Broken", (Tag)ByteTag.m_128273_((boolean)true));
        }
    }

    void rollback(CompoundTag compoundTag, Throwable throwable) {
        if (this.rollbackBase == null || this.finishing || throwable == null || !this.rollbackBase.m_128469_("tag").equals((Object)compoundTag)) {
            throw new IllegalStateException("Unknown original rollback baseline");
        }
        this.queue.clear();
        this.next = 0;
        this.rollbackAttempted = true;
        CompoundTag compoundTag2 = this.cursor.m_6426_();
        this.append(compoundTag2, 8, "", "", (Tag)this.rollbackBase.m_128469_("tag"));
    }

    void finish(boolean bl) {
        if (!this.started || this.finished || this.finishing) {
            throw new IllegalStateException("Detached hurt return");
        }
        this.fresh();
        this.finishing = true;
        CompoundTag compoundTag = this.cursor.m_6426_();
        this.ensure(compoundTag);
        if (bl && (OwnerStatueWearPlan.life(compoundTag).m_128451_("PendingMaterials") > 0 || OwnerStatueWearPlan.life(compoundTag).m_128451_("PendingSockets") > 0)) {
            this.append(compoundTag, 9, "FTBossWeaponLifecycle", "PendingMaterials", null);
            this.append(compoundTag, 9, "FTBossWeaponLifecycle", "PendingSockets", null);
        }
    }

    void finished() {
        if (!this.finishing || !this.complete()) {
            throw new IllegalStateException("Incomplete normal hurt return");
        }
        this.finished = true;
        ++this.cycles;
    }

    Step next(int n, String string, Tag tag) {
        if (this.rejected || this.complete()) {
            throw new IllegalStateException("Unplanned wear mutation");
        }
        Step step = this.queue.get(this.next);
        if (step.site != n || !step.key.equals(string) || !Objects.equals(step.value, tag)) {
            throw new IllegalStateException("Independent wear operation mismatch");
        }
        return step;
    }

    Step singleCarryStep() {
        Step step;
        block5: {
            block4: {
                Tag tag;
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerRepairContinuation.class && stackFrame.getMethodName().equals("prewrite")).orElse(false));
                if (!bl || this.rejected || !this.started || this.applied || this.finishing || this.finished || this.next != 0 || this.queue.size() != 1) {
                    throw new IllegalStateException("No independent single carry step");
                }
                step = this.queue.get(0);
                CompoundTag compoundTag = this.cursor.m_128469_("tag").m_128469_("FTBossWeaponMaterials");
                double d = compoundTag.m_128459_("WearCarry");
                if (step.site != 1 || !step.path.equals("FTBossWeaponMaterials") || !step.key.equals("WearCarry") || !((tag = step.value) instanceof DoubleTag)) break block4;
                DoubleTag doubleTag = (DoubleTag)tag;
                if (this.cursor.m_128469_("tag").m_128425_("FTBossWeaponMaterials", 10) && Double.isFinite(d) && Double.isFinite(doubleTag.m_7061_()) && !DoubleTag.m_128500_((double)d).equals((Object)doubleTag) && !this.cursor.equals((Object)step.after)) break block5;
            }
            throw new IllegalStateException("Absent or unchanged carry");
        }
        return step;
    }

    void advanced(Step step) {
        if (step != this.queue.get(this.next)) {
            throw new IllegalStateException("Wear step replay");
        }
        this.cursor = step.after.m_6426_();
        ++this.next;
    }

    int expectedMaximum() {
        return this.maximum(this.cursor);
    }

    boolean taskStartEligible(Throwable throwable) {
        return throwable == null && !this.rollbackAttempted && !this.swallowedRollback && this.terminal(null);
    }

    boolean terminal(Throwable throwable) {
        return !this.rejected && this.started && (throwable != null || this.finished && this.complete());
    }

    record Step(int site, String path, String key, Tag value, CompoundTag after) {
    }
}

