/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.common.ForgeMod
 *  top.theillusivec4.curios.api.SlotContext
 *  top.theillusivec4.curios.api.type.capability.ICurioItem
 */
package net.farmingtales.ftboss.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class BossRelicItem
extends Item
implements ICurioItem {
    private final BossRelicRules.Kind kind;

    public BossRelicItem(BossRelicRules.Kind kind) {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
        this.kind = kind;
    }

    public static ItemStack roll(Item item, RandomSource random) {
        ItemStack stack = new ItemStack((ItemLike)item);
        stack.m_41784_().m_128405_("ftboss:relic_affix", random.m_188503_(BossRelicRules.AFFIXES.size()));
        return stack;
    }

    public static int affix(ItemStack stack) {
        return stack.m_41782_() && stack.m_41783_().m_128425_("ftboss:relic_affix", 3) ? BossRelicRules.validAffix(stack.m_41783_().m_128451_("ftboss:relic_affix")) : -1;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID slotId, ItemStack stack) {
        HashMultimap modifiers = HashMultimap.create();
        if (context.cosmetic() || !"boss_relic".equals(context.identifier())) {
            return modifiers;
        }
        switch (this.kind) {
            case FROST: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22276_, 6.0, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case BUTCHER: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22281_, 3.0, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case TEA: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22284_, 4.0, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case WINE: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22278_, 0.3, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case RAT: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22279_, 0.12, AttributeModifier.Operation.MULTIPLY_BASE, "base");
                break;
            }
            case DEFECT: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22283_, 0.15, AttributeModifier.Operation.MULTIPLY_BASE, "base");
                break;
            }
            case COMPOST: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22285_, 3.0, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case APPLE: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22286_, 2.0, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case BENGER: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22279_, 0.08, AttributeModifier.Operation.MULTIPLY_BASE, "base");
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22281_, 1.0, AttributeModifier.Operation.ADDITION, "base");
                break;
            }
            case TIDE: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, (Attribute)ForgeMod.SWIM_SPEED.get(), 0.35, AttributeModifier.Operation.MULTIPLY_BASE, "base");
            }
        }
        switch (BossRelicItem.affix(stack)) {
            case 0: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22276_, 4.0, AttributeModifier.Operation.ADDITION, "affix");
                break;
            }
            case 1: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22284_, 2.0, AttributeModifier.Operation.ADDITION, "affix");
                break;
            }
            case 2: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22279_, 0.05, AttributeModifier.Operation.MULTIPLY_BASE, "affix");
                break;
            }
            case 3: {
                BossRelicItem.add((Multimap<Attribute, AttributeModifier>)modifiers, slotId, Attributes.f_22286_, 1.0, AttributeModifier.Operation.ADDITION, "affix");
                break;
            }
        }
        return modifiers;
    }

    private static void add(Multimap<Attribute, AttributeModifier> modifiers, UUID slotId, Attribute attribute, double amount, AttributeModifier.Operation operation, String part) {
        UUID modifierId = UUID.nameUUIDFromBytes(("ftboss:relic/" + slotId + "/" + part).getBytes(StandardCharsets.UTF_8));
        modifiers.put((Object)attribute, (Object)new AttributeModifier(modifierId, "ftboss.relic." + part, amount, operation));
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        return "boss_relic".equals(context.identifier());
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.boss_relic.slot").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)("tooltip.ftboss.boss_relic." + this.kind.item)).m_130940_(ChatFormatting.AQUA));
        int affix = BossRelicItem.affix(stack);
        tooltip.add((Component)Component.m_237115_((String)(affix < 0 ? "tooltip.ftboss.boss_relic.unrolled" : "tooltip.ftboss.boss_relic.affix." + BossRelicRules.AFFIXES.get(affix))).m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.boss_relic.source", (Object[])new Object[]{Component.m_237115_((String)("entity.ftboss." + this.kind.entity))}).m_130940_(ChatFormatting.DARK_GRAY));
    }
}

