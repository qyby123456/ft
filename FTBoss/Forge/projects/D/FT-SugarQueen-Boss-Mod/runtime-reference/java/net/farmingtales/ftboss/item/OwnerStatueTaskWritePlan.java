/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

final class OwnerStatueTaskWritePlan {
    final FourWeaponTransactions.Operation operation;
    final int index;
    final int level;
    final int maximum;
    final String profile;
    final List<Step> steps = new ArrayList<Step>();
    CompoundTag cursor;
    int next;
    private boolean submitted;

    OwnerStatueTaskWritePlan(CompoundTag compoundTag, FourWeaponTransactions.Operation operation, int n, int n2, int n3, String string) {
        this.cursor = compoundTag.m_6426_();
        this.operation = operation;
        this.index = n;
        this.level = n2;
        this.maximum = n3;
        this.profile = string;
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = OwnerStatueTaskWritePlan.life(compoundTag2);
        if (!compoundTag3.m_128403_("Weapon") || !compoundTag3.m_128425_("WearStep", 3) || string.isEmpty() || !string.equals(compoundTag3.m_128461_("RepairProfile")) || compoundTag2.m_128441_("ForgeCaps")) {
            throw new IllegalArgumentException("No task-origin ordinary input");
        }
        if (operation == FourWeaponTransactions.Operation.START_REPAIR) {
            if (compoundTag3.m_128441_("RepairQuest")) {
                throw new IllegalArgumentException("Task-origin START already has quest");
            }
            this.ensure(compoundTag2);
            CompoundTag compoundTag4 = new CompoundTag();
            compoundTag4.m_128405_("Version", 1);
            compoundTag4.m_128405_("Level", n2);
            compoundTag4.m_128359_("Profile", string);
            compoundTag4.m_128385_("Progress", new int[3]);
            this.append(compoundTag2, "FTBossWeaponLifecycle", "RepairQuest", (Tag)compoundTag4);
            this.revision(compoundTag2);
        } else if (operation == FourWeaponTransactions.Operation.CLAIM_REPAIR) {
            if (!FoodWeaponLifecycleRules.ready(compoundTag3.m_128469_("RepairQuest").m_128465_("Progress"), n2)) {
                throw new IllegalArgumentException("Task-origin claim not ready");
            }
            this.append(compoundTag2, "FTBossWeaponLifecycle", "RepairQuest", null);
            this.ensure(compoundTag2);
            this.append(compoundTag2, "", "Damage", (Tag)IntTag.m_128679_((int)0));
            this.append(compoundTag2, "FTBossWeaponLifecycle", "Broken", null);
            this.append(compoundTag2, "FTBossWeaponLifecycle", "PendingMaterials", null);
            this.append(compoundTag2, "FTBossWeaponLifecycle", "PendingSockets", null);
            this.append(compoundTag2, "FTBossWeaponLifecycle", "WearStep", (Tag)IntTag.m_128679_((int)0));
            if (compoundTag2.m_128469_("tag").m_128425_("FTBossWeaponMaterials", 10)) {
                this.append(compoundTag2, "FTBossWeaponMaterials", "WearCarry", (Tag)DoubleTag.m_128500_((double)0.0));
            }
            this.revision(compoundTag2);
        } else if (operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR) {
            throw new IllegalArgumentException("Not a task operation");
        }
    }

    private static CompoundTag life(CompoundTag compoundTag) {
        return compoundTag.m_128469_("tag").m_128469_("FTBossWeaponLifecycle");
    }

    private void append(CompoundTag compoundTag, String string, String string2, Tag tag) {
        CompoundTag compoundTag2 = compoundTag.m_128469_("tag");
        for (String string3 : string.isEmpty() ? new String[]{} : string.split("/")) {
            compoundTag2 = compoundTag2.m_128469_(string3);
        }
        if (tag == null) {
            compoundTag2.m_128473_(string2);
        } else {
            compoundTag2.m_128365_(string2, tag.m_6426_());
        }
        this.steps.add(new Step(string, string2, tag == null ? null : tag.m_6426_(), compoundTag.m_6426_()));
    }

    private void ensure(CompoundTag compoundTag) {
        if (this.level > 0) {
            this.append(compoundTag, "FTBossWeaponLifecycle", "WasEvolved", (Tag)ByteTag.m_128273_((boolean)true));
        }
        if (compoundTag.m_128469_("tag").m_128451_("Damage") >= this.maximum) {
            this.append(compoundTag, "FTBossWeaponLifecycle", "Broken", (Tag)ByteTag.m_128273_((boolean)true));
        }
    }

    private void revision(CompoundTag compoundTag) {
        int n = Math.max(0, OwnerStatueTaskWritePlan.life(compoundTag).m_128451_("RepairRevision"));
        this.append(compoundTag, "FTBossWeaponLifecycle", "RepairRevision", (Tag)IntTag.m_128679_((int)(n == Integer.MAX_VALUE ? 0 : n + 1)));
    }

    void submit(int n) {
        if (this.operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR || this.submitted || n <= 0 || this.index < 0 || this.index >= 3) {
            throw new IllegalArgumentException("Unverified task payment");
        }
        int[] nArray = OwnerStatueTaskWritePlan.life(this.cursor).m_128469_("RepairQuest").m_128465_("Progress");
        if (nArray.length != 3 || n > FoodWeaponLifecycleRules.repairRequired(this.level, this.index) - nArray[this.index]) {
            throw new IllegalArgumentException("Task payment exceeds independent need");
        }
        this.submitted = true;
        nArray[this.index] = Math.addExact(nArray[this.index], n);
        CompoundTag compoundTag = this.cursor.m_6426_();
        this.append(compoundTag, "FTBossWeaponLifecycle/RepairQuest", "Progress", (Tag)new IntArrayTag(nArray));
        Step step = this.steps.remove(this.steps.size() - 1);
        this.steps.add(new Step(step.path(), "ProgressArray", step.value(), step.after()));
        this.append(compoundTag, "FTBossWeaponLifecycle/RepairQuest", "Progress", (Tag)new IntArrayTag(nArray));
        this.revision(compoundTag);
    }

    Step expected(String string, Tag tag) {
        if (this.next >= this.steps.size()) {
            throw new IllegalStateException("Unplanned task write");
        }
        Step step = this.steps.get(this.next);
        if (!step.key.equals(string) || !Objects.equals(step.value, tag)) {
            throw new IllegalStateException("Independent task write mismatch");
        }
        return step;
    }

    void advance(Step step) {
        if (step != this.steps.get(this.next)) {
            throw new IllegalStateException("Task step replay");
        }
        this.cursor = step.after.m_6426_();
        ++this.next;
    }

    boolean complete() {
        return this.next == this.steps.size() && (this.operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR || this.submitted);
    }

    record Step(String path, String key, Tag value, CompoundTag after) {
    }
}

