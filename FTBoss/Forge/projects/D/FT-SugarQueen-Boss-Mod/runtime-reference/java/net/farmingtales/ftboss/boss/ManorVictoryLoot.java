/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.ManorWeaponCatalog;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

public final class ManorVictoryLoot {
    private static final String WEAPON_CHEST_NAME = "container.ftboss.manor_reward.weapons";
    private static final String SPELL_CHEST_NAME = "container.ftboss.manor_reward.spells";
    private static final String RECORD_CHEST_NAME = "container.ftboss.manor_reward.records";

    private ManorVictoryLoot() {
    }

    public static List<ItemStack> weaponContents() {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        for (ManorWeaponCatalog.Entry entry : ManorWeaponCatalog.ENTRIES) {
            ItemStack stack = ManorWeaponCatalog.stack(entry);
            if (!stack.m_41619_()) {
                result.add(stack);
                continue;
            }
            FTBossMod.LOGGER.warn("[FTBoss][ManorReward] Optional weapon unavailable: {}", (Object)entry.item());
        }
        result.add(ManorWeaponCatalog.stack(new ManorWeaponCatalog.Entry("ftboss:qingyi_confluence_trident", ManorWeaponCatalog.Form.DEFAULT)));
        result.removeIf(ItemStack::m_41619_);
        return result;
    }

    public static List<ItemStack> spellContents() {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        if (ModList.get().isLoaded("irons_spellbooks")) {
            ManorVictoryLoot.addScrolls(result);
        }
        result.removeIf(ItemStack::m_41619_);
        return result;
    }

    public static List<ItemStack> recordContents() {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        ManorVictoryLoot.addRecord(result, FTBossItems.MUSIC_DISC_MANOR_CUE);
        ManorVictoryLoot.addRecord(result, FTBossItems.MUSIC_DISC_MANOR_BATTLE);
        ManorVictoryLoot.addRecord(result, FTBossItems.MUSIC_DISC_MANOR_DUAL);
        ManorVictoryLoot.addRecord(result, FTBossItems.MUSIC_DISC_MANOR_GUIGNOL);
        ManorVictoryLoot.addRecord(result, FTBossItems.MUSIC_DISC_BENGER);
        for (RegistryObject<Item> record : FTBossItems.FOOD_MUSIC_DISCS) {
            ManorVictoryLoot.addRecord(result, record);
        }
        return result;
    }

    public static List<ItemStack> contents() {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        result.addAll(ManorVictoryLoot.weaponContents());
        result.addAll(ManorVictoryLoot.spellContents());
        result.addAll(ManorVictoryLoot.recordContents());
        return result;
    }

    public static List<ItemStack> packedChests(UUID victory) {
        return List.of(ManorVictoryLoot.packed(victory, ManorVictoryLoot.weaponContents(), WEAPON_CHEST_NAME), ManorVictoryLoot.packed(victory, ManorVictoryLoot.spellContents(), SPELL_CHEST_NAME), ManorVictoryLoot.packed(victory, ManorVictoryLoot.recordContents(), RECORD_CHEST_NAME));
    }

    private static void addScrolls(List<ItemStack> result) {
        for (ManorSpellRules.Kind kind : ManorSpellRules.Kind.values()) {
            ManorSignatureSpell spell = ManorIronSpells.spell(kind);
            if (!spell.isEnabled()) continue;
            result.add(ManorIronSpells.scroll(kind, spell.getMaxLevel()));
        }
    }

    private static void addRecord(List<ItemStack> result, RegistryObject<Item> record) {
        ItemStack stack = new ItemStack((ItemLike)record.get());
        if (!stack.m_41619_()) {
            result.add(stack);
        }
    }

    public static ItemStack packed(UUID victory, List<ItemStack> contents) {
        return ManorVictoryLoot.packed(victory, contents, "block.ftboss.manor_loot_chest");
    }

    private static ItemStack packed(UUID victory, List<ItemStack> contents, String nameKey) {
        if (contents.size() > 54) {
            throw new IllegalStateException("Manor reward category exceeds chest capacity: " + nameKey);
        }
        MutableComponent name = Component.m_237115_((String)nameKey);
        ItemStack chest = new ItemStack((ItemLike)FTBlocks.MANOR_LOOT_ITEM.get());
        chest.m_41714_((Component)name);
        NonNullList slots = NonNullList.m_122780_((int)54, (Object)ItemStack.f_41583_);
        for (int index = 0; index < contents.size(); ++index) {
            slots.set(index, (Object)contents.get(index).m_41777_());
        }
        CompoundTag inventory = new CompoundTag();
        inventory.m_128362_("ManorVictory", victory);
        inventory.m_128359_("CustomName", Component.Serializer.m_130703_((Component)name));
        ContainerHelper.m_18973_((CompoundTag)inventory, (NonNullList)slots);
        chest.m_41784_().m_128365_("BlockEntityTag", (Tag)inventory);
        return chest;
    }
}

