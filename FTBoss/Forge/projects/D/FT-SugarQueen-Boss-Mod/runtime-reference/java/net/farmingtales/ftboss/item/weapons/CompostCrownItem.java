/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class CompostCrownItem
extends SwordItem {
    public static final int POISON_TICKS = 80;

    public CompostCrownItem() {
        super((Tier)FTWeaponTier.INSTANCE, 10, -3.2f, new Item.Properties().m_41497_(Rarity.EPIC));
    }

    public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.m_7579_(stack, target, attacker);
        if (hit && !target.m_9236_().f_46443_ && target.m_6084_()) {
            target.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 80, 0), (Entity)attacker);
        }
        return hit;
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.compost_crown.origin").m_130940_(ChatFormatting.DARK_PURPLE));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.compost_crown.poison", (Object[])new Object[]{4}).m_130940_(ChatFormatting.GREEN));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.compost_crown.immunity").m_130940_(ChatFormatting.GRAY));
    }
}

