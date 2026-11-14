/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.saveddata.SavedData
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public final class FoodRewardClaims
extends SavedData {
    private final Set<UUID> claimed = new HashSet<UUID>();

    public static FoodRewardClaims load(CompoundTag compoundTag) {
        FoodRewardClaims foodRewardClaims = new FoodRewardClaims();
        ListTag listTag = compoundTag.m_128437_("Claims", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.m_128728_(i);
            if (!compoundTag2.m_128403_("Id")) continue;
            foodRewardClaims.claimed.add(compoundTag2.m_128342_("Id"));
        }
        return foodRewardClaims;
    }

    public boolean claim(UUID uUID) {
        if (!this.claimed.add(uUID)) {
            return false;
        }
        this.m_77762_();
        return true;
    }

    public CompoundTag m_7176_(CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (UUID uUID : this.claimed) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128362_("Id", uUID);
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_("Claims", (Tag)listTag);
        return compoundTag;
    }
}

