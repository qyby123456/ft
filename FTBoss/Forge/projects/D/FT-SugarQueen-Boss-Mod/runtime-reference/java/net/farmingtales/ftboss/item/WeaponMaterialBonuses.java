/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.event.ItemAttributeModifierEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class WeaponMaterialBonuses {
    private static final List<String> ATTRIBUTES = List.of("spell_power", "max_mana", "attack_damage", "armor", "max_health");
    private static final List<Map<String, UUID>> IDS = List.of(WeaponMaterialBonuses.ids("mainhand"), WeaponMaterialBonuses.ids("offhand"));

    private WeaponMaterialBonuses() {
    }

    private static Map<String, UUID> ids(String string) {
        HashMap<String, UUID> hashMap = new HashMap<String, UUID>();
        for (String string2 : ATTRIBUTES) {
            hashMap.put(string2, UUID.nameUUIDFromBytes(("ftboss.material_rarity." + string + "." + string2).getBytes(StandardCharsets.UTF_8)));
        }
        return Map.copyOf(hashMap);
    }

    private static boolean candidate(CompoundTag compoundTag) {
        block5: {
            block4: {
                if (compoundTag == null) {
                    return false;
                }
                if (!compoundTag.m_128425_("Cells", 9)) break block4;
                ListTag listTag = compoundTag.m_128437_("Cells", 10);
                if (listTag.size() > 27) {
                    return false;
                }
                for (int i = 0; i < listTag.size(); ++i) {
                    if (!WeaponMaterialBonusRules.relevant(listTag.m_128728_(i).m_128469_("Item").m_128461_("id"))) continue;
                    return true;
                }
                break block5;
            }
            if (!compoundTag.m_128425_("Cells", 10)) break block5;
            CompoundTag compoundTag2 = compoundTag.m_128469_("Cells");
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                if (!WeaponMaterialBonusRules.relevant(compoundTag2.m_128469_(slot.name()).m_128469_("Item").m_128461_("id"))) continue;
                return true;
            }
        }
        return false;
    }

    public static WeaponMaterialBonusRules.Input input(ItemStack itemStack) {
        String string;
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        String string2 = string = resourceLocation == null ? "" : resourceLocation.toString();
        if (itemStack.m_41619_() || itemStack.m_41613_() != 1 || itemStack.m_41763_() || !WeaponMaterialBonusRules.relevant(string)) {
            return null;
        }
        ItemStack itemStack2 = new ItemStack((ItemLike)itemStack.m_41720_());
        return new WeaponMaterialBonusRules.Input(string, itemStack2.m_41791_().name());
    }

    public static WeaponMaterialBonusRules.Bonus bonuses(ItemStack itemStack) {
        if (itemStack.m_41619_() || itemStack.m_41613_() != 1 || itemStack.m_41783_() == null || !WeaponMaterialBonuses.candidate(itemStack.m_41737_("FTBossFourWeaponFoundation")) && !WeaponMaterialBonuses.candidate(itemStack.m_41737_("FTBossSwordStructure"))) {
            return WeaponMaterialBonusRules.ZERO;
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle");
        if (compoundTag != null && compoundTag.m_128471_("Broken") || FoodWeaponLifecycle.durabilityDepleted(itemStack)) {
            return WeaponMaterialBonusRules.ZERO;
        }
        ArrayList<WeaponMaterialBonusRules.Input> arrayList = new ArrayList<WeaponMaterialBonusRules.Input>();
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
            if (!view.valid() || !view.status().ready()) {
                return WeaponMaterialBonusRules.ZERO;
            }
            view.entries().forEach((n, entry) -> {
                WeaponMaterialBonusRules.Input input;
                if (entry.paid() && FourWeaponFoundationRules.unlocked(view.profile(), n, view.level()) && (input = WeaponMaterialBonuses.input(entry.storedItem())) != null) {
                    arrayList.add(input);
                }
            });
        } else if (SwordCavityBuild.applies(itemStack) && !SwordCavityBuild.missingRequired(itemStack)) {
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                WeaponMaterialBonusRules.Input input;
                if (!SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal()) || (input = WeaponMaterialBonuses.input(SwordCavityBuild.storedItem(itemStack, slot))) == null) continue;
                arrayList.add(input);
            }
        }
        return WeaponMaterialBonusRules.combine(arrayList);
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent itemAttributeModifierEvent) {
        if (itemAttributeModifierEvent.getSlotType() != EquipmentSlot.MAINHAND && itemAttributeModifierEvent.getSlotType() != EquipmentSlot.OFFHAND) {
            return;
        }
        WeaponMaterialBonusRules.Bonus bonus = WeaponMaterialBonuses.bonuses(itemAttributeModifierEvent.getItemStack());
        if (bonus.equals(WeaponMaterialBonusRules.ZERO)) {
            return;
        }
        int n = itemAttributeModifierEvent.getSlotType() == EquipmentSlot.MAINHAND ? 0 : 1;
        WeaponMaterialBonuses.add(itemAttributeModifierEvent, (Attribute)ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "spell_power")), n, "spell_power", bonus.spellPower());
        WeaponMaterialBonuses.add(itemAttributeModifierEvent, (Attribute)ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "max_mana")), n, "max_mana", bonus.maxMana());
        WeaponMaterialBonuses.add(itemAttributeModifierEvent, Attributes.f_22281_, n, "attack_damage", bonus.attackDamage());
        WeaponMaterialBonuses.add(itemAttributeModifierEvent, Attributes.f_22284_, n, "armor", bonus.armor());
        WeaponMaterialBonuses.add(itemAttributeModifierEvent, Attributes.f_22276_, n, "max_health", bonus.maxHealth());
    }

    private static void add(ItemAttributeModifierEvent itemAttributeModifierEvent, Attribute attribute, int n, String string, double d) {
        if (attribute != null && d > 0.0) {
            itemAttributeModifierEvent.addModifier(attribute, new AttributeModifier(IDS.get(n).get(string), "ftboss.material_rarity." + string, d, AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent
    public static void materialTooltip(ItemTooltipEvent itemTooltipEvent) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemTooltipEvent.getItemStack().m_41720_());
        if (resourceLocation == null) {
            return;
        }
        if (resourceLocation.toString().equals("kubejs:ne_ingot")) {
            itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.material.diamond_alloy.durability", (Object[])new Object[]{Math.round(10.0), Math.round(200.0), String.format(Locale.ROOT, "%.1f", WeaponDurabilityRules.multiplier(Integer.MAX_VALUE))}).m_130940_(ChatFormatting.AQUA));
            itemTooltipEvent.getToolTip().add(Component.m_237115_((String)"tooltip.ftboss.material.diamond_alloy.requirements").m_130940_(ChatFormatting.DARK_GRAY));
            return;
        }
        if (!WeaponMaterialBonusRules.relevant(resourceLocation.toString())) {
            return;
        }
        ItemStack itemStack = new ItemStack((ItemLike)itemTooltipEvent.getItemStack().m_41720_());
        WeaponMaterialBonusRules.Input input = WeaponMaterialBonuses.input(itemStack);
        if (input == null) {
            return;
        }
        WeaponMaterialBonusRules.Bonus bonus = WeaponMaterialBonusRules.material(input.identifier(), input.rarity());
        String string = bonus.spellPower() > 0.0 ? String.format(Locale.ROOT, "\u914d\u4ef6\u52a0\u6210\uff1a\u6cd5\u5f3a +%.0f%% / \u84dd\u91cf +%.0f", bonus.spellPower() * 100.0, bonus.maxMana()) : String.format(Locale.ROOT, "\u914d\u4ef6\u52a0\u6210\uff1a\u653b\u51fb +%.1f / \u62a4\u7532 +%.2f / \u751f\u547d +%.0f", bonus.attackDamage(), bonus.armor(), bonus.maxHealth());
        itemTooltipEvent.getToolTip().add(Component.m_237113_((String)string).m_130940_(ChatFormatting.AQUA));
        itemTooltipEvent.getToolTip().add(Component.m_237113_((String)"\u4ec5\u88c5\u5165\u53ef\u7528\u6b66\u5668\u540e\u751f\u6548\uff1b\u540c\u79cd\u6750\u6599\u53ea\u8ba1\u4e00\u6b21\uff0c\u5355\u4ef6\u6b66\u5668\u52a0\u6210\u6709\u4e0a\u9650").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

