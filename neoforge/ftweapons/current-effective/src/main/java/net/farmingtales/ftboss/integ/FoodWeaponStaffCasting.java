package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public final class FoodWeaponStaffCasting {
    private FoodWeaponStaffCasting() {
    }

    private static ItemStack weapon(MagicData var0, CastSource var1) {
        if (var1 != CastSource.SPELLBOOK && var1 != CastSource.SWORD) {
            return ItemStack.EMPTY;
        } else {
            ItemStack var2 = var0.getPlayerCastingItem();
            return var2 != null && FoodWeaponUseBridge.kind(var2) == Kind.STAFF ? var2 : ItemStack.EMPTY;
        }
    }

    public static boolean canFinish(ServerPlayer var0, CastSource var1) {
        return FoodWeaponRanged.canCast(var0, weapon(MagicData.getPlayerMagicData(var0), var1));
    }

    public static void prepareCost(Event var0, ServerPlayer var1, CastSource var2) {
        if (var0 instanceof SpellOnCastEvent var3) {
            ItemStack var4 = weapon(MagicData.getPlayerMagicData(var1), var2);
            if (!var4.isEmpty() && FoodWeaponRanged.canCast(var1, var4)) {
                int var5 = var3.getManaCost();
                if (var5 > 0) {
                    var3.setManaCost((int)Math.max(1L, Math.min(2147483647L, (long)Math.ceil((double)var5 * FoodWeaponFamilyBuild.manaMultiplier(var4)))));
                }
            }
        }
    }

    public static void cast(AbstractSpell var0, Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
        if (var3 instanceof ServerPlayer var6) {
            ItemStack var7 = weapon(var5, var4);
            FoodWeaponRanged.withStaffCast(
                var6,
                var7,
                1 + Math.max(0, Math.min(30, var2)) / 5,
                FoodWeaponSpellSession.sourceSnapshot(var5),
                () -> var0.onCast(var1, var2, var3, var4, var5)
            );
        } else {
            var0.onCast(var1, var2, var3, var4, var5);
        }
    }
}
