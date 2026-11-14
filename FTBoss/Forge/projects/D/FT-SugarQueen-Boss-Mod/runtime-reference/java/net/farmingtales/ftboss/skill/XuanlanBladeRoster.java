/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.skill;

import java.util.UUID;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class XuanlanBladeRoster {
    private static final String[] KEYS = new String[]{"ftboss:XuanlanBlade", "ftboss:XuanlanBlade2"};

    private XuanlanBladeRoster() {
    }

    private static boolean valid(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        return blade.m_6084_() && !blade.m_213877_() && blade.m_9236_() == owner.m_284548_() && blade.m_19749_() != null && owner.m_20148_().equals(blade.m_19749_().m_20148_()) && owner.m_284548_().m_8791_(blade.m_20148_()) == blade;
    }

    public static int count(ServerPlayer owner) {
        CompoundTag data = owner.getPersistentData();
        UUID first = null;
        int count = 0;
        for (String key : KEYS) {
            PlayerMoonChakramEntity blade;
            Entity entity;
            if (!data.m_128403_(key)) {
                data.m_128473_(key);
                continue;
            }
            UUID identifier = data.m_128342_(key);
            if (identifier.equals(first) || !((entity = owner.m_284548_().m_8791_(identifier)) instanceof PlayerMoonChakramEntity) || !XuanlanBladeRoster.valid(owner, blade = (PlayerMoonChakramEntity)entity)) {
                data.m_128473_(key);
                continue;
            }
            first = identifier;
            ++count;
        }
        return count;
    }

    public static int slot(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        if (!XuanlanBladeRoster.valid(owner, blade)) {
            return -1;
        }
        CompoundTag data = owner.getPersistentData();
        for (int index = 0; index < KEYS.length; ++index) {
            if (!data.m_128403_(KEYS[index]) || !data.m_128342_(KEYS[index]).equals(blade.m_20148_())) continue;
            return index;
        }
        return -1;
    }

    public static int track(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        if (!XuanlanBladeRoster.valid(owner, blade)) {
            return -1;
        }
        int count = XuanlanBladeRoster.count(owner);
        int existing = XuanlanBladeRoster.slot(owner, blade);
        if (existing >= 0) {
            return existing;
        }
        if (count >= 2) {
            return -1;
        }
        for (int index = 0; index < KEYS.length; ++index) {
            if (owner.getPersistentData().m_128403_(KEYS[index])) continue;
            owner.getPersistentData().m_128362_(KEYS[index], blade.m_20148_());
            return index;
        }
        return -1;
    }

    public static int forget(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        int found = -1;
        CompoundTag data = owner.getPersistentData();
        for (int index = 0; index < KEYS.length; ++index) {
            if (!data.m_128403_(KEYS[index]) || !data.m_128342_(KEYS[index]).equals(blade.m_20148_())) continue;
            data.m_128473_(KEYS[index]);
            if (found >= 0) continue;
            found = index;
        }
        return found;
    }
}

