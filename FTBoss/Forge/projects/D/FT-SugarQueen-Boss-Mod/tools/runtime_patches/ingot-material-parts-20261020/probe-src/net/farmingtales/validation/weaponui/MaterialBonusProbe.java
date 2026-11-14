package net.farmingtales.validation.weaponui;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.item.WeaponMaterialBonuses;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;

/** 冷启动生产包中的真实属性事件回归，不直接调用订阅器冒充生效。 */
public final class MaterialBonusProbe {
    private static int checks;
    private static int cases;
    private static double maxReadMillis;
    private static long totalReadNanos;
    private static int equippedChecks;
    private static void require(boolean value, String label) { checks++; if (!value) throw new IllegalStateException(label); }
    public static void installed(ItemStack weapon, ItemStack material) {
        var input = WeaponMaterialBonuses.input(material);
        Bonus expected = input == null ? WeaponMaterialBonusRules.ZERO : WeaponMaterialBonusRules.material(input.identifier(), input.rarity());
        CompoundTag original = weapon.save(new CompoundTag());
        long start = System.nanoTime();
        Bonus actualBonus = WeaponMaterialBonuses.bonuses(weapon);
        require(actualBonus.equals(expected), "Installed material bonuses " + material + " expected=" + expected + " actual=" + actualBonus + " weapon=" + weapon.save(new CompoundTag()));
        totalReadNanos += System.nanoTime() - start;
        maxReadMillis = Math.max(maxReadMillis, (System.nanoTime() - start) / 1000000.0);
        List<Attribute> attributes = List.of(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "spell_power")),
                ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "max_mana")), Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.MAX_HEALTH);
        double[] amounts = {expected.spellPower(), expected.maxMana(), expected.attackDamage(), expected.armor(), expected.maxHealth()};
        Set<UUID> mainIds = new HashSet<>();
        for (EquipmentSlot slot : List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD)) {
            var modifiers = weapon.getAttributeModifiers(slot);
            for (int index = 0; index < attributes.size(); index++) {
                double actual = 0.0;
                for (AttributeModifier modifier : modifiers.get(attributes.get(index))) {
                    if (!modifier.getName().startsWith("ftboss.material_rarity.")) continue;
                    require(modifier.getOperation() == AttributeModifier.Operation.ADDITION, "Operation");
                    if (slot == EquipmentSlot.MAINHAND) mainIds.add(modifier.getId());
                    if (slot == EquipmentSlot.OFFHAND) require(!mainIds.contains(modifier.getId()), "Hands share UUID");
                    actual += modifier.getAmount();
                }
                require(Math.abs(actual - (slot == EquipmentSlot.HEAD ? 0.0 : amounts[index])) < 0.0000001, "Auto-subscribed attribute " + slot + " " + material + " " + index + " " + actual);
            }
        }
        require(original.equals(weapon.save(new CompoundTag())), "Attribute reads mutated NBT");
        ItemStack invalid = weapon.copy(); invalid.setCount(2);
        require(WeaponMaterialBonuses.bonuses(invalid).equals(WeaponMaterialBonusRules.ZERO), "Count guard");
        ItemStack broken = weapon.copy(); broken.getOrCreateTagElement(FoodWeaponLifecycle.ROOT).putBoolean("Broken", true);
        require(WeaponMaterialBonuses.bonuses(broken).equals(WeaponMaterialBonusRules.ZERO), "Broken guard");
        if (weapon.getTagElement(FourWeaponFoundationAdapter.ROOT) != null) {
            ItemStack malformed = weapon.copy(); malformed.getTagElement(FourWeaponFoundationAdapter.ROOT).putInt("Schema", 99);
            require(WeaponMaterialBonuses.bonuses(malformed).equals(WeaponMaterialBonusRules.ZERO), "Schema guard");
        }
        require(WeaponMaterialBonuses.bonuses(material).equals(WeaponMaterialBonusRules.ZERO), "Holding raw material gives bonus");
        cases++;
    }
    public static void removed(ItemStack weapon) {
        require(WeaponMaterialBonuses.bonuses(weapon).equals(WeaponMaterialBonusRules.ZERO), "Removal invalidation");
    }
    public static boolean equipped(net.minecraft.server.level.ServerPlayer player, ItemStack weapon, Path instancePath) {
        Bonus bonus = WeaponMaterialBonuses.bonuses(weapon);
        List<Attribute> attributes = List.of(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "spell_power")),
                ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "max_mana")), Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.MAX_HEALTH);
        double[] amounts = {bonus.spellPower(), bonus.maxMana(), bonus.attackDamage(), bonus.armor(), bonus.maxHealth()};
        for (int index = 0; index < attributes.size(); index++) {
            var instance = player.getAttribute(attributes.get(index));
            if (instance == null) return false;
            double actual = instance.getModifiers().stream().filter(modifier -> modifier.getName().startsWith("ftboss.material_rarity."))
                    .mapToDouble(AttributeModifier::getAmount).sum();
            if (Math.abs(actual - amounts[index]) > 0.0000001) return false;
        }
        equippedChecks++;
        if (equippedChecks == 36) {
            try {
                Files.writeString(instancePath.resolve("evidence/EQUIPPED_BONUS_RESULT.json"),
                        new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status", "PASS_PLAYER_ATTRIBUTES_INSTALL_AND_REMOVE", "checks", equippedChecks)));
            } catch (Exception problem) { throw new IllegalStateException(problem); }
        }
        return true;
    }
    public static void finish(Path instance) {
        try {
            Files.writeString(instance.resolve("evidence/MATERIAL_BONUS_RESULT.json"), new GsonBuilder().setPrettyPrinting().create().toJson(
                    Map.of("status", "PASS_REAL_ATTRIBUTE_EVENTS_RARITY_AND_INVALIDATION", "cases", cases, "checks", checks, "maxReadMillis", maxReadMillis, "meanReadMillis", totalReadNanos / 1000000.0 / Math.max(1, cases))));
        } catch (Exception problem) { throw new IllegalStateException(problem); }
    }
}
