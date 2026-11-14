/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponSpellSession {
    private static final Map<MagicData, Session> SESSIONS = new WeakHashMap<MagicData, Session>();

    private FoodWeaponSpellSession() {
    }

    public static synchronized void bind(MagicData magicData, LivingEntity livingEntity, ItemStack itemStack, CastSource castSource) {
        ServerPlayer serverPlayer;
        block3: {
            block2: {
                FoodWeaponSpellSession.clear(magicData);
                if (!(livingEntity instanceof ServerPlayer)) break block2;
                serverPlayer = (ServerPlayer)livingEntity;
                if ((castSource == CastSource.SPELLBOOK || castSource == CastSource.SWORD) && FoodWeaponUseBridge.kind(itemStack) == FoodWeaponFamilyRules.Kind.STAFF && FoodWeaponRanged.canCast(serverPlayer, itemStack)) break block3;
            }
            return;
        }
        SESSIONS.put(magicData, new Session(serverPlayer, itemStack, FoodWeaponRanged.captureBound(serverPlayer, itemStack, FoodWeaponFamilyRules.Kind.STAFF), castSource, magicData.getCastingSpellId()));
    }

    public static synchronized void clear(MagicData magicData) {
        SESSIONS.remove(magicData);
    }

    public static synchronized void clearOwner(ServerPlayer serverPlayer) {
        SESSIONS.values().removeIf(session -> session.player().m_20148_().equals(serverPlayer.m_20148_()) && session.player().m_284548_().m_7654_() == serverPlayer.m_284548_().m_7654_());
    }

    public static synchronized void clearServer(MinecraftServer minecraftServer) {
        SESSIONS.values().removeIf(session -> session.player().m_284548_().m_7654_() == minecraftServer);
    }

    private static synchronized CompoundTag snapshot(MagicData magicData) {
        Session session = SESSIONS.get(magicData);
        return session != null && FoodWeaponSourceLedger.validReadOnly(session.snapshot(), session.player()) && magicData.getPlayerCastingItem() == session.weapon() && magicData.getCastSource() == session.source() && session.spellId().equals(magicData.getCastingSpellId()) ? session.snapshot().m_6426_() : new CompoundTag();
    }

    public static CompoundTag sourceSnapshot(MagicData magicData) {
        return FoodWeaponSpellSession.snapshot(magicData);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void callback(MagicData magicData, LivingEntity livingEntity, boolean bl, Runnable runnable) {
        ServerPlayer serverPlayer;
        ServerPlayer serverPlayer2 = livingEntity instanceof ServerPlayer ? (serverPlayer = (ServerPlayer)livingEntity) : null;
        try {
            FoodWeaponRanged.withSource(serverPlayer2, FoodWeaponSpellSession.snapshot(magicData), () -> {
                runnable.run();
                return null;
            });
        }
        finally {
            if (bl) {
                FoodWeaponSpellSession.clear(magicData);
            }
        }
    }

    private record Session(ServerPlayer player, ItemStack weapon, CompoundTag snapshot, CastSource source, String spellId) {
    }
}

