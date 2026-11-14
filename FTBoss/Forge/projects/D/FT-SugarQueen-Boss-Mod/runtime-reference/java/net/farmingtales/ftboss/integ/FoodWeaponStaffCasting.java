/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SpellOnCastEvent
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.eventbus.api.Event
 */
package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.farmingtales.ftboss.integ.FoodWeaponSpellSession;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public final class FoodWeaponStaffCasting {
    private FoodWeaponStaffCasting() {
    }

    private static ItemStack weapon(MagicData magicData, CastSource castSource) {
        if (castSource != CastSource.SPELLBOOK && castSource != CastSource.SWORD) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack = magicData.getPlayerCastingItem();
        return itemStack != null && FoodWeaponUseBridge.kind(itemStack) == FoodWeaponFamilyRules.Kind.STAFF ? itemStack : ItemStack.f_41583_;
    }

    public static boolean canFinish(ServerPlayer serverPlayer, CastSource castSource) {
        return FoodWeaponRanged.canCast(serverPlayer, FoodWeaponStaffCasting.weapon(MagicData.getPlayerMagicData((LivingEntity)serverPlayer), castSource));
    }

    public static void prepareCost(Event event, ServerPlayer serverPlayer, CastSource castSource) {
        if (!(event instanceof SpellOnCastEvent)) {
            return;
        }
        SpellOnCastEvent spellOnCastEvent = (SpellOnCastEvent)event;
        ItemStack itemStack = FoodWeaponStaffCasting.weapon(MagicData.getPlayerMagicData((LivingEntity)serverPlayer), castSource);
        if (itemStack.m_41619_() || !FoodWeaponRanged.canCast(serverPlayer, itemStack)) {
            return;
        }
        int n = spellOnCastEvent.getManaCost();
        if (n > 0) {
            spellOnCastEvent.setManaCost((int)Math.max(1L, Math.min(Integer.MAX_VALUE, (long)Math.ceil((double)n * FoodWeaponFamilyBuild.manaMultiplier(itemStack)))));
        }
    }

    public static void cast(AbstractSpell abstractSpell, Level level, int n, LivingEntity livingEntity, CastSource castSource, MagicData magicData) {
        if (!(livingEntity instanceof ServerPlayer)) {
            abstractSpell.onCast(level, n, livingEntity, castSource, magicData);
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
        ItemStack itemStack = FoodWeaponStaffCasting.weapon(magicData, castSource);
        FoodWeaponRanged.withStaffCast(serverPlayer, itemStack, 1 + Math.max(0, Math.min(30, n)) / 5, FoodWeaponSpellSession.sourceSnapshot(magicData), () -> abstractSpell.onCast(level, n, livingEntity, castSource, magicData));
    }
}

