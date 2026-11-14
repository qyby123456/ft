/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
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
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class FoodAppraisalMonocleItem
extends Item
implements ICurioItem {
    public static final String SLOT = "monocle";

    public FoodAppraisalMonocleItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE));
    }

    public static boolean isEquipped(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        try {
            return CuriosApi.getCuriosHelper().findCurios(entity, stack -> stack.m_150930_((Item)FTBossItems.FOOD_APPRAISAL_MONOCLE.get())).stream().anyMatch(result -> SLOT.equals(result.slotContext().identifier()) && !result.slotContext().cosmetic());
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        return SLOT.equals(context.identifier()) && !context.cosmetic();
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.food_appraisal_monocle.slot").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.food_appraisal_monocle.effect").m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.food_appraisal_monocle.clean").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

