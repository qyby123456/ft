package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/** 可选铁魔法法杖桥；2026-10-13 只处理真实施法物品，书/卷轴/指令不借用当前主手，也不改法术来源。 */
public final class FoodWeaponStaffCasting {
    private FoodWeaponStaffCasting() {}

    private static ItemStack weapon(MagicData data, CastSource source) {
        if (source != CastSource.SPELLBOOK && source != CastSource.SWORD) return ItemStack.EMPTY;
        ItemStack stack = data.getPlayerCastingItem();
        return stack != null && FoodWeaponFamilyBuild.kind(stack) == Kind.STAFF ? stack : ItemStack.EMPTY;
    }

    public static boolean canFinish(ServerPlayer player, CastSource source) {
        return FoodWeaponRanged.canCast(player, weapon(MagicData.getPlayerMagicData(player), source));
    }

    public static void prepareCost(Event event, ServerPlayer player, CastSource source) {
        if (!(event instanceof SpellOnCastEvent cast)) return;
        ItemStack weapon = weapon(MagicData.getPlayerMagicData(player), source);
        if (weapon.isEmpty() || !FoodWeaponRanged.canCast(player, weapon)) return;
        int cost = cast.getManaCost();
        if (cost > 0) cast.setManaCost((int) Math.max(1L, Math.min(Integer.MAX_VALUE,
                (long) Math.ceil(cost * FoodWeaponFamilyBuild.manaMultiplier(weapon)))));
    }

    public static void cast(AbstractSpell spell, Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        if (!(caster instanceof ServerPlayer player)) {
            spell.onCast(level, spellLevel, caster, source, data);
            return;
        }
        ItemStack weapon = weapon(data, source);
        FoodWeaponRanged.withStaffCast(player, weapon, 1 + Math.max(0, Math.min(30, spellLevel)) / 5,
                () -> spell.onCast(level, spellLevel, caster, source, data));
    }
}
