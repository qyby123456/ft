package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.ManorWeaponCatalog;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

public final class ManorVictoryLoot {
    private static final String WEAPON_CHEST_NAME = "container.ftboss.manor_reward.weapons";
    private static final String SPELL_CHEST_NAME = "container.ftboss.manor_reward.spells";
    private static final String RECORD_CHEST_NAME = "container.ftboss.manor_reward.records";

    private ManorVictoryLoot() {}

    public static List<ItemStack> weaponContents() {
        List<ItemStack> result = new ArrayList<>();
        for (ManorWeaponCatalog.Entry entry : ManorWeaponCatalog.ENTRIES) {
            ItemStack stack = ManorWeaponCatalog.stack(entry);
            if (!stack.isEmpty()) result.add(stack);
            else FTBossMod.LOGGER.warn("[FTBoss][ManorReward] Optional weapon unavailable: {}", entry.item());
        }
        result.add(ManorWeaponCatalog.stack(new ManorWeaponCatalog.Entry(
                "ftboss:qingyi_confluence_trident", ManorWeaponCatalog.Form.DEFAULT)));
        result.removeIf(ItemStack::isEmpty);
        return result;
    }

    public static List<ItemStack> spellContents() {
        List<ItemStack> result = new ArrayList<>();
        if (ModList.get().isLoaded("irons_spellbooks")) addScrolls(result);
        result.removeIf(ItemStack::isEmpty);
        return result;
    }

    public static List<ItemStack> recordContents() {
        List<ItemStack> result = new ArrayList<>();
        addRecord(result, FTBossItems.MUSIC_DISC_MANOR_CUE);
        addRecord(result, FTBossItems.MUSIC_DISC_MANOR_BATTLE);
        addRecord(result, FTBossItems.MUSIC_DISC_MANOR_DUAL);
        addRecord(result, FTBossItems.MUSIC_DISC_MANOR_GUIGNOL);
        addRecord(result, FTBossItems.MUSIC_DISC_BENGER);
        for (RegistryObject<net.minecraft.world.item.Item> record : FTBossItems.FOOD_MUSIC_DISCS) {
            addRecord(result, record);
        }
        return result;
    }

    /** Kept for callers that need the complete reward list rather than the three packed chests. */
    public static List<ItemStack> contents() {
        List<ItemStack> result = new ArrayList<>();
        result.addAll(weaponContents());
        result.addAll(spellContents());
        result.addAll(recordContents());
        return result;
    }

    public static List<ItemStack> packedChests(UUID victory) {
        return List.of(
                packed(victory, weaponContents(), WEAPON_CHEST_NAME),
                packed(victory, spellContents(), SPELL_CHEST_NAME),
                packed(victory, recordContents(), RECORD_CHEST_NAME));
    }

    private static void addScrolls(List<ItemStack> result) {
        for (var kind : net.farmingtales.ftboss.util.ManorSpellRules.Kind.values()) {
            var spell = net.farmingtales.ftboss.integ.spells.ManorIronSpells.spell(kind);
            if (spell.isEnabled()) {
                result.add(net.farmingtales.ftboss.integ.spells.ManorIronSpells.scroll(kind, spell.getMaxLevel()));
            }
        }
    }

    private static void addRecord(List<ItemStack> result, RegistryObject<net.minecraft.world.item.Item> record) {
        ItemStack stack = new ItemStack(record.get());
        if (!stack.isEmpty()) result.add(stack);
    }

    public static ItemStack packed(UUID victory, List<ItemStack> contents) {
        return packed(victory, contents, "block.ftboss.manor_loot_chest");
    }

    private static ItemStack packed(UUID victory, List<ItemStack> contents, String nameKey) {
        if (contents.size() > ManorLootChestBlockEntity.CAPACITY) {
            throw new IllegalStateException("Manor reward category exceeds chest capacity: " + nameKey);
        }

        Component name = Component.translatable(nameKey);
        ItemStack chest = new ItemStack(FTBlocks.MANOR_LOOT_ITEM.get());
        chest.setHoverName(name);
        NonNullList<ItemStack> slots = NonNullList.withSize(ManorLootChestBlockEntity.CAPACITY, ItemStack.EMPTY);
        for (int index = 0; index < contents.size(); index++) slots.set(index, contents.get(index).copy());

        CompoundTag inventory = new CompoundTag();
        inventory.putUUID("ManorVictory", victory);
        inventory.putString("CustomName", Component.Serializer.toJson(name));
        ContainerHelper.saveAllItems(inventory, slots);
        chest.getOrCreateTag().put("BlockEntityTag", inventory);
        return chest;
    }
}