/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.WeaponBossMaterialRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class WeaponBossMaterialItem
extends Item {
    private final BossRelicRules.Kind kind;

    public WeaponBossMaterialItem(BossRelicRules.Kind kind) {
        super(new Item.Properties().m_41487_(64).m_41497_(Rarity.RARE).m_41486_());
        this.kind = kind;
    }

    public void m_7373_(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.boss_material", (Object[])new Object[]{Component.m_237115_((String)("material.ftboss.weapon." + WeaponBossMaterialRules.material(this.kind).id()))}).m_130940_(ChatFormatting.AQUA));
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.boss_material.source", (Object[])new Object[]{Component.m_237115_((String)("entity.ftboss." + this.kind.entity))}).m_130940_(ChatFormatting.DARK_GRAY));
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.boss_material.consume").m_130940_(ChatFormatting.GRAY));
    }
}

