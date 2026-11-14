/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.saveddata.SavedData
 */
package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

final class OwnerStatueTaskSavedData
extends SavedData {
    static final String NAME = "ftboss_repair_tasks";
    private CompoundTag owners = new CompoundTag();
    private CompoundTag cutover = new CompoundTag();

    OwnerStatueTaskSavedData() {
    }

    static OwnerStatueTaskSavedData load(CompoundTag compoundTag) {
        if (compoundTag.m_128451_("Schema") != 1) {
            throw new IllegalArgumentException("Unsupported repair SavedData schema");
        }
        OwnerStatueTaskSavedData ownerStatueTaskSavedData = new OwnerStatueTaskSavedData();
        ownerStatueTaskSavedData.owners = compoundTag.m_128469_("Owners").m_6426_();
        ownerStatueTaskSavedData.cutover = compoundTag.m_128469_("Cutover").m_6426_();
        return ownerStatueTaskSavedData;
    }

    boolean hadCutover() {
        return !this.cutover.m_128456_();
    }

    void cutover(CompoundTag compoundTag) {
        this.cutover = compoundTag.m_6426_();
        this.m_77762_();
    }

    void owner(UUID uUID, CompoundTag compoundTag) {
        this.owners.m_128365_(uUID.toString(), (Tag)compoundTag.m_6426_());
        this.m_77762_();
    }

    public CompoundTag m_7176_(CompoundTag compoundTag) {
        compoundTag.m_128405_("Schema", 1);
        compoundTag.m_128365_("Owners", (Tag)this.owners.m_6426_());
        compoundTag.m_128365_("Cutover", (Tag)this.cutover.m_6426_());
        return compoundTag;
    }
}

