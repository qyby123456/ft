/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  top.theillusivec4.curios.api.CuriosApi
 *  top.theillusivec4.curios.api.SlotContext
 *  top.theillusivec4.curios.api.type.capability.ICurioItem
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import javax.annotation.Nullable;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class SilverKeyItem
extends Item
implements ICurioItem {
    public static final int BLOOM_REFRESH_TICKS = 60;

    public SilverKeyItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
    }

    public static boolean isEquipped(LivingEntity entity) {
        try {
            return CuriosApi.getCuriosHelper().findCurios(entity, stack -> stack.m_150930_((Item)FTBossItems.SILVER_KEY.get())).stream().anyMatch(result -> "boss_relic".equals(result.slotContext().identifier()) && !result.slotContext().cosmetic());
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity wearer = context.entity();
        if (context.cosmetic() || !"boss_relic".equals(context.identifier()) || wearer.m_9236_().f_46443_ || wearer.f_19797_ % 20 != 0) {
            return;
        }
        MobEffectInstance bloom = wearer.m_21124_((MobEffect)FTEffects.BLOOM.get());
        if (bloom == null || bloom.m_19557_() <= 30) {
            wearer.m_7292_(new MobEffectInstance((MobEffect)FTEffects.BLOOM.get(), 60, 0, true, false, true));
        }
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (!context.entity().m_9236_().f_46443_) {
            context.entity().m_21195_((MobEffect)FTEffects.BLOOM.get());
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        return "boss_relic".equals(context.identifier());
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.silver_key.slot").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.silver_key.vision").m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.silver_key.thorns").m_130940_(ChatFormatting.DARK_RED));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.silver_key.bloom").m_130940_(ChatFormatting.GREEN));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.silver_key.source").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

