package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponSpellSession {
    private static final Map<MagicData, FoodWeaponSpellSession.Session> SESSIONS = new WeakHashMap<>();

    private FoodWeaponSpellSession() {
    }

    public static synchronized void bind(MagicData var0, LivingEntity var1, ItemStack var2, CastSource var3) {
        clear(var0);
        if (var1 instanceof ServerPlayer var4
            && (var3 == CastSource.SPELLBOOK || var3 == CastSource.SWORD)
            && FoodWeaponUseBridge.kind(var2) == Kind.STAFF
            && FoodWeaponRanged.canCast(var4, var2)) {
            SESSIONS.put(
                var0, new FoodWeaponSpellSession.Session(var4, var2, FoodWeaponRanged.captureBound(var4, var2, Kind.STAFF), var3, var0.getCastingSpellId())
            );
            return;
        }
    }

    public static synchronized void clear(MagicData var0) {
        SESSIONS.remove(var0);
    }

    public static synchronized void clearOwner(ServerPlayer var0) {
        SESSIONS.values()
            .removeIf(var1 -> var1.player().getUUID().equals(var0.getUUID()) && var1.player().serverLevel().getServer() == var0.serverLevel().getServer());
    }

    public static synchronized void clearServer(MinecraftServer var0) {
        SESSIONS.values().removeIf(var1 -> var1.player().serverLevel().getServer() == var0);
    }

    private static synchronized CompoundTag snapshot(MagicData var0) {
        FoodWeaponSpellSession.Session var1 = SESSIONS.get(var0);
        return var1 != null
                && FoodWeaponSourceLedger.validReadOnly(var1.snapshot(), var1.player())
                && var0.getPlayerCastingItem() == var1.weapon()
                && var0.getCastSource() == var1.source()
                && var1.spellId().equals(var0.getCastingSpellId())
            ? var1.snapshot().copy()
            : new CompoundTag();
    }

    public static CompoundTag sourceSnapshot(MagicData var0) {
        return snapshot(var0);
    }

    public static void callback(MagicData var0, LivingEntity var1, boolean var2, Runnable var3) {
        ServerPlayer var4 = var1 instanceof ServerPlayer var5 ? var5 : null;

        try {
            FoodWeaponRanged.withSource(var4, snapshot(var0), () -> {
                var3.run();
                return null;
            });
        } finally {
            if (var2) {
                clear(var0);
            }
        }
    }

    private static record Session(ServerPlayer player, ItemStack weapon, CompoundTag snapshot, CastSource source, String spellId) {
    }
}
